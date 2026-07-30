package ru.practicum.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.AnalyzerClient;
import ru.practicum.client.CollectorClient;
import ru.practicum.client.RecommendedEvent;
import ru.practicum.client.StatClient;
import ru.practicum.constants.Constants;
import ru.practicum.event.dto.event.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.*;
import ru.practicum.event.state.*;
import ru.practicum.ewm.StatRequestParamDto;
import ru.practicum.ewm.StatResponseDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.CreationRulesException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.repository.category.CategoryRepository;
import ru.practicum.repository.event.EventRepository;
import ru.practicum.repository.location.LocationRepository;
import ru.practicum.request.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.user.UserClient;
import ru.practicum.user.UserMapper;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.stats.service.collector.UserActionOuterClass.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final UserClient userClient;
    private final RequestClient requestClient;
    private final StatClient statClient; // Исключено
    private final AnalyzerClient analyzerClient;
    private final CollectorClient collectorClient;

    @Transactional
    @Override
    public EventFullDto addEvent(Long initiatorId, NewEventDto dto) {
        Category category = categoryRepository.findById(dto.getCategory()).orElseThrow(() -> new NotFoundException(
                "Добавление события. Категория с ID: " + dto.getCategory() + " не найдена."));

        Location location = locationRepository.save(LocationMapper.dtoToLocation(dto.getLocation()));

        LocalDateTime eventDate = LocalDateTime.parse(dto.getEventDate(), Constants.FORMATTER);

        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            log.error("Добавление события. " +
                    "Время начала события должно быть не ранее, чем через два часа от текущего момента.");
            throw new ValidationException("Время начала события должно быть не ранее, " +
                    "чем через два часа от текущего момента.");
        }

        if (dto.getPaid() == null) {
            dto.setPaid(false);
        }

        if (dto.getParticipantLimit() == null) {
            dto.setParticipantLimit(0);
        }

        if (dto.getRequestModeration() == null) {
            dto.setRequestModeration(true);
        }

        Event newEvent = EventMapper.dtoToEvent(
                dto,
                category,
                LocalDateTime.now(),
                initiatorId,
                location,
                null,
                EventState.PENDING
        );

        newEvent.setCompilations(new ArrayList<>());

        Event addedEvent = eventRepository.save(newEvent);
        return EventMapper.eventToFullDto(
                addedEvent,
                UserMapper.toUserShortDto(userClient.findUserById(initiatorId)),
                0L,
                0.0
        );
    }

    @Override
    public List<EventShortDto> getEventsOfUser(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorOrderByEventDateAsc(userId, pageable);
        if (events == null || events.isEmpty()) {
            log.info("Получение списка событий пользователя. " +
                    "По заданным параметрам (userId: {}, from: {}, size {}) события не найдены.", userId, from, size);
            return new ArrayList<>();
        }
        Map<Long, Long> confirmedRequestsCount = getConfirmedRequestsCount(events);
        Map<Long, Double> ratingsStats = getRatingsCount(events);
        Map<Long, UserShortDto> initiators = userClient.findAllUsers(Collections.singletonList(userId));

        return EventMapper.eventToShortDto(
                events,
                initiators,
                confirmedRequestsCount,
                ratingsStats
        );
    }

    @Override
    public EventFullDto getEventById(Long initiatorId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(
                "Получения данных о событии. Событие с ID: " + eventId + " не найдено."));

        if (!event.getInitiator().equals(initiatorId)) {
            log.error("Получение данных о событии. " +
                    "Пользователь с ID: {} не является инициатором события с ID: {}", initiatorId, eventId);
            throw new NotFoundException("Пользователь с ID: " + initiatorId +
                    " не является инициатором события с ID: " + eventId);
        }

        Map<Long, Double> ratingsStats = getRatingsCount(List.of(event));
        Double ratings = ratingsStats.getOrDefault(event.getId(), 0.0);
        Long confirmedRequests = getConfirmedRequestsCount(List.of(event)).getOrDefault(event.getId(), 0L);
        return EventMapper.eventToFullDto(
                event,
                UserMapper.toUserShortDto(userClient.findUserById(initiatorId)),
                confirmedRequests,
                ratings
        );
    }

    @Override
    public List<EventShortDto> getShortEventsInfoByIds(List<Long> eventIds) {
        List<Event> events = eventRepository.findAllByIdInOrderByIdAsc(eventIds);

        if (events == null || events.isEmpty()) {
            log.info("Получение событий по списку ID. По указанным в списке ID событий события не найдены.");
            return new ArrayList<>();
        }

        Map<Long, Long> confirmedRequestsCount = getConfirmedRequestsCount(events);
        Map<Long, Double> viewsStats = getRatingsCount(events);
        Map<Long, UserShortDto> initiators = userClient.findAllUsers(
                events.stream()
                        .map(Event::getInitiator)
                        .toList()
        );

        return EventMapper.eventToShortDto(
                events,
                initiators,
                confirmedRequestsCount,
                viewsStats
        );
    }

    @Transactional
    @Override
    public EventFullDto patchEventById(Long userId, Long eventId, UpdateEventUserRequest dto) {
        Event oldEvent = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(
                "Обновление данных события. Событие с ID: " + eventId + " не найдено."));

        if (!oldEvent.getInitiator().equals(userId)) {
            log.error("Обновление данных события. " +
                    "Пользователь с ID: {} не является инициатором события с ID: {}", userId, eventId);
            throw new NotFoundException("Пользователь с ID: " + userId +
                    " не является инициатором события с ID: " + eventId);
        }

        if (oldEvent.getState().equals(EventState.PUBLISHED)) {
            log.error("Обновление данных события. Изменить можно только отмененные события " +
                    "или события в состоянии ожидания модерации.");
            throw new CreationRulesException("Изменить можно только отмененные события " +
                    "или события в состоянии ожидания модерации.");
        }

        if (dto.getEventDate() != null) {
            LocalDateTime eventDate = LocalDateTime.parse(dto.getEventDate(), Constants.FORMATTER);

            if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                log.error("Обновление данных события. Время начала события должно быть не ранее, " +
                        "чем через два часа от текущего момента.");
                throw new ValidationException("Время начала события должно быть не ранее, " +
                        "чем через два часа от текущего момента.");
            }

            oldEvent.setEventDate(eventDate);
        }

        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory()).orElseThrow(() -> new NotFoundException(
                    "Обновление данных события. Категория с ID: " + dto.getCategory() + " не найдена."));

            oldEvent.setCategory(category);
        }

        if (dto.getLocation() != null) {
            oldEvent.getLocation().setLat(dto.getLocation().getLat());
            oldEvent.getLocation().setLon(dto.getLocation().getLon());
        }

        if (dto.getAnnotation() != null) {
            oldEvent.setAnnotation(dto.getAnnotation());
        }

        if (dto.getDescription() != null) {
            oldEvent.setDescription(dto.getDescription());
        }

        if (dto.getPaid() != null) {
            oldEvent.setPaid(dto.getPaid());
        }

        if (dto.getParticipantLimit() != null) {
            oldEvent.setParticipantLimit(dto.getParticipantLimit());
        }

        if (dto.getRequestModeration() != null) {
            oldEvent.setRequestModeration(dto.getRequestModeration());
        }

        if (dto.getStateAction() != null) {
            if (dto.getStateAction().equals(UserStateAction.SEND_TO_REVIEW.toString())) {
                oldEvent.setState(EventState.PENDING);
            }
            if (dto.getStateAction().equals(UserStateAction.CANCEL_REVIEW.toString())) {
                oldEvent.setState(EventState.CANCELED);
            }
        }

        if (dto.getTitle() != null) {
            oldEvent.setTitle(dto.getTitle());
        }

        Event patchedEvent = eventRepository.save(oldEvent);

        Map<Long, Double> viewsStats = getRatingsCount(List.of(patchedEvent));
        Double ratings = viewsStats.getOrDefault(patchedEvent.getId(), 0.0);
        Long confirmedRequests = getConfirmedRequestsCount(List.of(patchedEvent)).getOrDefault(patchedEvent.getId(), 0L);

        return EventMapper.eventToFullDto(
                patchedEvent,
                UserMapper.toUserShortDto(userClient.findUserById(patchedEvent.getInitiator())),
                confirmedRequests,
                ratings
        );
    }

    @Override
    public List<ParticipationRequestDto> getRequestsOfEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(
                "Получение запросов на участие в событии. Событие с ID: " + eventId + " не найдено."));
        if (!event.getInitiator().equals(userId)) {
            log.error("Получение запросов на участие в событии. " +
                    "Пользователь с ID: {} не является инициатором события с ID: {}", userId, eventId);
            throw new NotFoundException("Пользователь с ID: " + userId +
                    " не является инициатором события с ID: " + eventId);
        }

        List<ParticipationRequest> requests = requestClient.findAllByEventId(eventId);
        if (requests == null || requests.isEmpty()) {
            log.info("Получение запросов на участие в событии. По событию с ID: {} запросов не найдено.", eventId);
            return new ArrayList<>();
        }
        return requests.stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult patchRequestsStatusOfEvent(Long userId, Long eventId,
                                                                     EventRequestStatusUpdateRequest dto) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(
                "Обновление данных события. Событие с ID: " + eventId + " не найдено."));

        if (!event.getInitiator().equals(userId)) {
            log.error("Обновление статусов заявок. " +
                    "Пользователь с ID: {} не является инициатором события с ID: {}", userId, eventId);
            throw new NotFoundException("Пользователь с ID: " +
                    userId + " не является инициатором события с ID: " + eventId);
        }

        Long participantLimit = event.getParticipantLimit().longValue();
        Long approvedRequestsCount = requestClient.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        if (participantLimit > 0 && approvedRequestsCount >= participantLimit) {
            log.error("Обновление статусов заявок. " +
                    "Достигнут лимит одобренных заявок ({}), нельзя подтвердить больше.", approvedRequestsCount);
            throw new ConflictException("Достигнут лимит участников события");
        }
        List<ParticipationRequest> allRequests = requestClient.findAllById(dto.getRequestIds());

        if (allRequests.size() != dto.getRequestIds().size()) {
            log.error("Обновление статусов заявок. Некоторые заявки не найдены.");
            throw new NotFoundException("Некоторые заявки не найдены");
        }

        for (ParticipationRequest request : allRequests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                log.error("Обновление статусов заявок. Заявка с ID: {} имеет статус {}, а не PENDING",
                        request.getId(), request.getStatus());
                throw new CreationRulesException("Статус можно изменить только у заявок, " +
                        "находящихся в состоянии ожидания: " + RequestStatus.PENDING +
                        ". Текущий статус заявки: " + request.getStatus());
            }
        }

        List<ParticipationRequest> requests = allRequests;

        if (dto.getStatus() == RequestStatus.REJECTED) {
            // Отклоняем все заявки
            for (ParticipationRequest request : requests) {
                request.setStatus(RequestStatus.REJECTED);
            }
            requestClient.saveAll(requests);
            List<ParticipationRequestDto> resultRejectedRequestsDto = requests.stream()
                    .map(RequestMapper::toParticipationRequestDto)
                    .toList();
            return new EventRequestStatusUpdateResult(List.of(), resultRejectedRequestsDto);
        }

        if (dto.getRequestIds().size() != requests.size()) {
            if (participantLimit.equals(approvedRequestsCount) && participantLimit > 0) {
                log.error("Обновление статусов заявок на участие в событии. " +
                        "Достигнут лимит одобренных заявок в событии с ID: {}.", eventId);
                throw new CreationRulesException("Достигнут лимит одобренных заявок в событии с ID: " + eventId + ".");
            }
        }

        List<ParticipationRequest> approvedRequests = new ArrayList<>();
        List<ParticipationRequest> rejectedRequests = new ArrayList<>();

        for (ParticipationRequest request : requests) {
            if (approvedRequestsCount < participantLimit || participantLimit == 0) {
                request.setStatus(RequestStatus.CONFIRMED);
                approvedRequests.add(request);
                approvedRequestsCount++;
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
            }
        }

        requestClient.saveAll(approvedRequests);
        requestClient.saveAll(rejectedRequests);

        List<ParticipationRequestDto> resultApprovedRequestsDto = approvedRequests.stream()
                .map(RequestMapper::toParticipationRequestDto)
                .toList();
        List<ParticipationRequestDto> resultRejectedRequestsDto = rejectedRequests.stream()
                .map(RequestMapper::toParticipationRequestDto)
                .toList();

        return new EventRequestStatusUpdateResult(resultApprovedRequestsDto, resultRejectedRequestsDto);
    }

    @Override
    public List<EventFullDto> getEventsByAdminRequest(AdminEventRequestParam param) {
        log.info("Уровень Admin. Получение списка событий по параметрам: users={}, states={}, categories={}, " +
                        "rangeStart={}, rangeEnd={}, from={}, size={}",
                param.getUsers(), param.getStates(), param.getCategories(),
                param.getRangeStart(), param.getRangeEnd(), param.getFrom(), param.getSize());

        List<Event> events = new ArrayList<>();
        Pageable pageable = PageRequest.of(param.getFrom() / param.getSize(), param.getSize());

        if (param.getUsers() == null) {
            events = eventRepository.findAllUsers(pageable);
        } else if (param.getUsers().isEmpty() || param.getCategories().isEmpty()) {
            log.info("Уровень Admin. Переданы невалидные ID (0 или отрицательные). Возвращаем пустой список.");
            return new ArrayList<>();
        } else {
            events = eventRepository.findByAdminRequest(param, pageable);
        }

        if (events == null || events.isEmpty()) {
            log.info("Уровень Admin. Получение списка событий. По заданным параметрам события не найдены.");
            return new ArrayList<>();
        }

        Map<Long, Long> confirmedRequestsCount = getConfirmedRequestsCount(events); // Подтверждённые <eventId, confirmedRequestsCount>
        Map<Long, Double> ratingsStats = getRatingsCount(events); // Просмотры <eventId, countViews>
        Map<Long, UserShortDto> initiators = userClient.findAllUsers(events.stream()
                .map(Event::getInitiator)
                .toList()); // списки используемых пользователей

        return EventMapper.eventToFullDto(
                events,
                initiators,
                confirmedRequestsCount,
                ratingsStats
        );
    }

    @Transactional
    @Override
    public EventFullDto patchEventByIdByAdmin(Long eventId, UpdateEventAdminRequest dto) {
        Event oldEvent = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(
                "Уровень Admin. Обновление данных события. Событие с ID: " + eventId + " не найдено."));

        log.info("Публикация события. Текущий статус: {}, eventDate: {}",
                oldEvent.getState(), oldEvent.getEventDate());

        if (dto.getStateAction() != null) {
            if (dto.getStateAction().equals(AdminStateAction.PUBLISH_EVENT.toString())
                    && oldEvent.getState().equals(EventState.PENDING)) {
                oldEvent.setState(EventState.PUBLISHED);
                oldEvent.setPublishedOn(LocalDateTime.now());
            } else if (dto.getStateAction().equals(AdminStateAction.REJECT_EVENT.toString())
                    && !oldEvent.getState().equals(EventState.PUBLISHED)) {
                oldEvent.setState(EventState.CANCELED);
            } else {
                log.error("Уровень Admin. Обновление данных события. " +
                        "Опубликовать можно только событие, ожидающее публикации. " +
                        "Отклонить можно только событие, которое не опубликовано.");
                throw new CreationRulesException("Обновление данных события администратором. " +
                        "Опубликовать можно только событие, ожидающее публикации. " +
                        "Отклонить можно только событие, которое не опубликовано.");
            }
        }

        if (dto.getEventDate() != null) {
            LocalDateTime eventDate = LocalDateTime.parse(dto.getEventDate(), Constants.FORMATTER);

            if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
                log.error("Уровень Admin. Обновление данных события. " +
                        "Время начала события должно быть не ранее, чем через час от текущего момента.");
                throw new ValidationException("Обновление данных события администратором. " +
                        "Время начала события должно быть не ранее, чем через час от текущего момента.");
            }

            oldEvent.setEventDate(eventDate);
        }

        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory()).orElseThrow(() -> new NotFoundException(
                    "Обновление данных события администратором. " +
                            "Категория с ID: " + dto.getCategory() + " не найдена."));

            oldEvent.setCategory(category);
        }

        if (dto.getLocation() != null) {
            if (oldEvent.getLocation() != null) {
                oldEvent.getLocation().setLat(dto.getLocation().getLat());
                oldEvent.getLocation().setLon(dto.getLocation().getLon());
            } else {
                Location newLocation = locationRepository.save(LocationMapper.dtoToLocation(dto.getLocation()));
                oldEvent.setLocation(newLocation);
            }
        }

        if (dto.getAnnotation() != null) {
            oldEvent.setAnnotation(dto.getAnnotation());
        }

        if (dto.getDescription() != null) {
            oldEvent.setDescription(dto.getDescription());
        }

        if (dto.getPaid() != null) {
            oldEvent.setPaid(dto.getPaid());
        }

        if (dto.getParticipantLimit() != null) {
            oldEvent.setParticipantLimit(dto.getParticipantLimit());
        }

        if (dto.getRequestModeration() != null) {
            oldEvent.setRequestModeration(dto.getRequestModeration());
        }

        if (dto.getTitle() != null) {
            oldEvent.setTitle(dto.getTitle());
        }

        Event patchedEvent = eventRepository.save(oldEvent);
        log.info("После сохранения: статус = {}, publishedOn = {}",
                patchedEvent.getState(), patchedEvent.getPublishedOn());

        Double ratings = getRatingsCount(List.of(patchedEvent)).getOrDefault(patchedEvent.getId(), 0.0);
        Long confirmedRequests = getConfirmedRequestsCount(List.of(patchedEvent)).getOrDefault(patchedEvent.getId(), 0L);

        return EventMapper.eventToFullDto(
                patchedEvent,
                UserMapper.toUserShortDto(userClient.findUserById(patchedEvent.getInitiator())),
                confirmedRequests,
                ratings
        );
    }

    @Override
    public List<EventShortDto> getEventsByPublicRequest(PublicEventRequestParam param) {
        log.info("Публичный поиск событий: {}", param);

        Integer from = param.getFrom() != null ? param.getFrom() : 0;
        Integer size = param.getSize() != null && param.getSize() > 0 ? param.getSize() : 10;

        if (size <= 0) {
            size = 10;
        }
        if (from < 0) {
            from = 0;
        }

        LocalDateTime rangeStart = null;
        LocalDateTime rangeEnd = null;

        if (param.getRangeStart() != null && !param.getRangeStart().isBlank()) {
            rangeStart = LocalDateTime.parse(param.getRangeStart(), Constants.FORMATTER);
        }
        if (param.getRangeEnd() != null && !param.getRangeEnd().isBlank()) {
            rangeEnd = LocalDateTime.parse(param.getRangeEnd(), Constants.FORMATTER);
        }

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Дата начала диапазона не может быть позже даты окончания");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findByPublicRequest(param, pageable);

        if (events == null || events.isEmpty()) {
            log.info("Уровень Public. Получение списка событий. По заданным параметрам " +
                    "(param: {}, from: {}, size {}) события не найдены.", param, from, size);
            return new ArrayList<>();
        }

        Map<Long, Long> confirmedRequestsCount = getConfirmedRequestsCount(events);

        if (Boolean.TRUE.equals(param.getOnlyAvailable())) {
            Predicate<Event> isAvailable = event -> event.getParticipantLimit() >
                    confirmedRequestsCount.getOrDefault(event.getId(), 0L);
            events = events.stream()
                    .filter(isAvailable)
                    .toList();
        }

        Map<Long, Double> ratingsStats = getRatingsCount(events);
        Map<Long, UserShortDto> initiators = userClient.findAllUsers(
                events.stream()
                        .map(Event::getInitiator)
                        .toList()
        );

        List<EventShortDto> result = EventMapper.eventToShortDto(
                events,
                initiators,
                confirmedRequestsCount,
                ratingsStats
        );

        String sort = param.getSort();
        if (sort != null && sort.equalsIgnoreCase("views")) {
            return result.stream()
                    .sorted(Comparator.comparingDouble(EventShortDto::getRating).reversed())
                    .toList();
        } else {
            return result;
        }
    }

    @Override
    public EventFullDto getEventByIdByPublicRequest(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(
                "Публичный запрос на получение данных о событии. Событие с ID: " + eventId + " не найдено."));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            log.error("Уровень Public. Можно получить данные только события со статусом {}.", EventState.PUBLISHED);
            throw new NotFoundException("Публичный запрос на получение данных о событии. " +
                    "Можно получить данные только опубликованного события.");
        }

        try {
            collectorClient.collectUserAction(
                    event.getInitiator(),
                    eventId,
                    ActionTypeProto.ACTION_VIEW
            );
            log.info("Отправлен просмотр события {} от пользователя {}", eventId, event.getInitiator());
        } catch (Exception e) {
            log.error("Ошибка отправки просмотра в Collector: {}", e.getMessage(), e);
        }

        Double ratings = getRatingsCount(List.of(event)).getOrDefault(event.getId(), 0.0);
        Long confirmedRequests = getConfirmedRequestsCount(List.of(event)).getOrDefault(event.getId(), 0L);
        return EventMapper.eventToFullDto(
                event,
                UserMapper.toUserShortDto(userClient.findUserById(event.getInitiator())),
                confirmedRequests,
                ratings
        );
    }

    @Override
    public List<EventShortDto> getRecommendedEvents(Long userId, int maxResults) {
        List<RecommendedEvent> recommendations = analyzerClient.getRecommendationsForUser(userId, maxResults);

        if (recommendations == null || recommendations.isEmpty()) {
            log.info("Нет рекомендаций для пользователя {}", userId);
            return new ArrayList<>();
        }

        List<Long> eventIds = recommendations.stream()
                .map(RecommendedEvent::getEventId)
                .collect(Collectors.toList());

        List<Event> events = eventRepository.findAllByIdInAndState(eventIds, EventState.PUBLISHED);

        if (events.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, Double> ratings = getRatingsCount(events);

        // Получить количество подтвержденных запросов
        Map<Long, Long> confirmedRequests = getConfirmedRequestsCount(events);

        // Получить инициаторов событий
        Map<Long, UserShortDto> initiators = userClient.findAllUsers(
                events.stream()
                        .map(Event::getInitiator)
                        .collect(Collectors.toList())
        );

        List<EventShortDto> result = EventMapper.eventToShortDto(
                events,
                initiators,
                confirmedRequests,
                ratings
        );

        // Сортировка по рейтингу (от высокого к низкому)
        result.sort((a, b) -> Double.compare(b.getRating(), a.getRating()));

        log.info("Возвращено {} рекомендаций для пользователя {}", result.size(), userId);
        return result;
    }

    @Override
    @Transactional
    public void likeEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("Нельзя поставить лайк неопубликованному событию");
        }

        // Проверить, не ставил ли пользователь уже лайк этому событию
        List<RecommendedEvent> interactions = analyzerClient.getInteractionsCount(List.of(eventId));
        boolean alreadyLiked = interactions.stream()
                .anyMatch(re -> re.getEventId().equals(eventId) && re.getScore() >= 5.0);

        if (alreadyLiked) {
            log.warn("Пользователь {} уже поставил лайк событию {}", userId, eventId);
            throw new ValidationException("Вы уже поставили лайк этому мероприятию");
        }

        // Отправляем лайк в Collector
        try {
            collectorClient.collectUserAction(
                    userId,
                    eventId,
                    ActionTypeProto.ACTION_LIKE
            );
            log.info("Лайк на событие {} от пользователя {} отправлен в Collector", eventId, userId);
        } catch (Exception e) {
            log.error("Ошибка отправки лайка в Collector: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось отправить лайк", e);
        }
    }

    private Map<Long, Long> getViewsCount(List<Event> events) {
        if (events.isEmpty()) return Collections.emptyMap();

        List<String> uris = new ArrayList<>();
        for (Event event : events) {
            String uri = "/events/" + event.getId();
            uris.add(uri);
        }

        LocalDateTime earliestCratedEvent = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusDays(1));

        StatRequestParamDto statRequestParamDto = new StatRequestParamDto(
                earliestCratedEvent.minusMinutes(1).format(Constants.FORMATTER),
                LocalDateTime.now().plusMinutes(1).format(Constants.FORMATTER),
                uris,
                true
        );

        List<StatResponseDto> stats = statClient.getStats(statRequestParamDto);

        Map<Long, Long> result = new HashMap<>();
        for (StatResponseDto dto : stats) {
            Long eventId = Long.valueOf(dto.getUri().replace("/events/", ""));
            Long views = dto.getHits();
            result.put(eventId, views);
        }
        return result;
    }

    private Map<Long, Double> getRatingsCount(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        List<RecommendedEvent> recommendedEvents = analyzerClient.getInteractionsCount(eventIds);

        return recommendedEvents.stream()
                .collect(Collectors.toMap(
                        RecommendedEvent::getEventId,
                        RecommendedEvent::getScore,
                        (a, b) -> a  // обработка дублей
                ));
    }

    private Map<Long, Long> getConfirmedRequestsCount(List<Event> events) {
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        List<ConfirmedRequestCount> count = requestClient
                .countConfirmedRequestsByEventIds(eventIds, RequestStatus.CONFIRMED);

        return count.stream()
                .collect(Collectors.toMap(
                        ConfirmedRequestCount::getEventId,
                        ConfirmedRequestCount::getCount
                ));
    }

}