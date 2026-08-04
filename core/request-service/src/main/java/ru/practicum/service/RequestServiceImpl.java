package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.CollectorClient;
import ru.practicum.event.client.EventClient;
import ru.practicum.event.model.Event;
import ru.practicum.event.state.EventState;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.repository.RequestRepository;
import ru.practicum.request.ParticipationRequest;
import ru.practicum.request.RequestMapper;
import ru.practicum.request.RequestStatus;
import ru.practicum.request.dto.CreateUpdateRequestDto;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.stats.service.collector.UserActionOuterClass.ActionTypeProto;
import ru.practicum.user.User;
import ru.practicum.user.UserClient;
import ru.practicum.user.UserMapper;
import ru.practicum.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserClient userClient;
    private final EventClient eventClient;
    private final CollectorClient collectorClient;

    @Transactional
    @Override
    public ParticipationRequestDto createRequest(CreateUpdateRequestDto dto) {
        //Дата создания
        LocalDateTime now = LocalDateTime.now();
        //Получение сущностей для создания связей через JPA
        Event event = findEvent(dto.getEventId());
        User requester = UserMapper.toEntity(findUser(dto.getUserId()));

        //Проверка, что событие опубликовано
        if (event.getState() != EventState.PUBLISHED) {
            log.error("Не удается создать запрос на неопубликованное событие с id={}", event.getId());
            throw new ConflictException("Событие еще не опубликовано");
        }
        //Проверка, что инициатор не пытается участвовать в своем событии
        if (event.getInitiator().equals(requester.getId())) {
            log.error("Инициатор не может участвовать в собственном мероприятии. eventId={}, userId={}",
                    event.getId(), requester.getId());
            throw new ConflictException("Инициатор не может участвовать в собственном мероприятии");
        }

        //Проверка, что пользователь уже не создавал запрос
        Optional<ParticipationRequest> existingRequest =
                requestRepository.findByRequesterAndEvent(requester.getId(), event.getId());

        if (existingRequest.isPresent()) {
            log.error("Запрос пользователя {} на событие {} уже существует",
                    requester.getId(), event.getId());
            throw new ConflictException(String.format("Запрос пользователя c id=%d на событие c id=%d уже существует",
                    requester.getId(), event.getId()));
        }

        // Проверка лимита участников
        Long approvedRequestsCount = requestRepository.countByEventAndStatus(
                event.getId(), RequestStatus.CONFIRMED);

        if (event.getParticipantLimit() > 0 && approvedRequestsCount >= event.getParticipantLimit()) {
            log.error("Достигнут лимит участников для event {}. Limit: {}, CONFIRMED: {}",
                    event.getId(), event.getParticipantLimit(), approvedRequestsCount);
            throw new ConflictException(String.format("Достигнут лимит участников. Limit=%d, Approved=%d",
                    event.getParticipantLimit(), approvedRequestsCount));
        }
        //Определение статуса запроса
        RequestStatus initialStatus;

        if (event.getParticipantLimit() == 0) {
            initialStatus = RequestStatus.CONFIRMED;
        } else if (!event.getRequestModeration()) {
            initialStatus = RequestStatus.CONFIRMED;
        } else {
            initialStatus = RequestStatus.PENDING;
        }

        try {
            collectorClient.collectUserAction(
                    requester.getId(),
                    event.getId(),
                    ActionTypeProto.ACTION_REGISTER
            );
            log.info("Регистрация на событие {} от пользователя {} отправлена в Collector",
                    event.getId(), requester.getId());
        } catch (Exception e) {
            log.error("Ошибка отправки регистрации в Collector: {}", e.getMessage(), e);
        }

        ParticipationRequest request = RequestMapper.toEntity(now, event.getId(), requester.getId(), initialStatus);
        ParticipationRequest saved = requestRepository.save(request);
        log.info("Создан запрос с id={}, статус={}", saved.getId(), initialStatus);
        return RequestMapper.toParticipationRequestDto(saved);
    }

    @Override
    public List<ParticipationRequestDto> getRequestByUserId(Long userId) {
        log.info("Получение запросов пользователя с id={}", userId);

        checkUser(userId); // проверка существования

        return requestRepository.findAllByUserId(userId)
                .stream()
                .map(RequestMapper::toParticipationRequestDto)
                .toList();
    }

    @Transactional
    @Override
    public ParticipationRequestDto canceledRequest(Long userId, Long requestId) {
        log.info("Отмена запроса: userId={}, requestId={}", userId, requestId);

        checkUser(userId); // проверка существования

        ParticipationRequest request = findParticipationRequest(requestId);

        // Проверка, что запрос принадлежит пользователю
        if (!request.getRequester().equals(userId)) {
            log.error("Запрос с id={} не принадлежит пользователю с id={}", requestId, userId);
            throw new NotFoundException("Запрос не найден или не принадлежит пользователю");
        }

        // Только PENDING запросы можно отменить
        if (request.getStatus() != RequestStatus.PENDING) {
            log.error("Нельзя отменить запрос со статусом: {}", request.getStatus());
            throw new ConflictException("Можно отменить только запросы в статусе PENDING");
        }
        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest canceled = requestRepository.save(request);
        log.info("Запрос с id={} отменен", requestId);
        return RequestMapper.toParticipationRequestDto(canceled);
    }

    //Получение пользователя
    private UserDto findUser(Long userId) {
        return userClient.findUserById(userId); // не известно проверяется ли отсутствие пользователя
    }

    //Получение события
    private Event findEvent(Long eventId) {
        return eventClient.findById(eventId);
    }

    //Получение запроса
    private ParticipationRequest findParticipationRequest(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException("Request with id " + requestId + " not found")
        );
    }

    private void checkUser(long userId) {
        if(!userClient.existsByUserId(userId))
            throw new NotFoundException("Не существует пользователя с id: " + userId);
    }
}