package ru.practicum.event.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.constants.Constants;
import ru.practicum.event.dto.event.EventFullDto;
import ru.practicum.event.dto.event.EventShortDto;
import ru.practicum.event.dto.event.NewEventDto;
import ru.practicum.event.model.Category;
import ru.practicum.user.User;
import ru.practicum.event.model.Event;
import ru.practicum.event.state.EventState;
import ru.practicum.event.model.Location;
import ru.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@UtilityClass
public class EventMapper {

    public Event dtoToEvent(
            NewEventDto eventDto,
            Category category,
            LocalDateTime createdOn,
            Long initiator,
            Location location,
            LocalDateTime publishedOn,
            EventState state
    ) {
        return Event.builder()
                .annotation(eventDto.getAnnotation())
                .category(category)
                .createdOn(createdOn)
                .description(eventDto.getDescription())
                .eventDate(LocalDateTime.parse(eventDto.getEventDate(), Constants.FORMATTER))
                .initiator(initiator)
                .location(location)
                .paid(eventDto.getPaid())
                .participantLimit(eventDto.getParticipantLimit())
                .publishedOn(publishedOn)
                .requestModeration(eventDto.getRequestModeration())
                .state(state)
                .title(eventDto.getTitle())
                .build();
    }

    public EventFullDto eventToFullDto(
            Event event,
            UserShortDto initiator,
            Long confirmedRequests,
            Long views
    ) {
        return EventFullDto.builder()
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .createdOn(event.getCreatedOn().format(Constants.FORMATTER))
                .description(event.getDescription())
                .eventDate(event.getEventDate().format(Constants.FORMATTER))
                .id(event.getId())
                .initiator(initiator)
                .location(LocationMapper.locationToDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn() != null ? event
                        .getPublishedOn().format(Constants.FORMATTER) : null)
                .requestModeration(event.getRequestModeration())
                .state(event.getState().toString())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public List<EventFullDto> eventToFullDto(
            List<Event> event,
            Map<Long, UserShortDto> initiators,
            Map<Long, Long> confirmedRequests, // <eventId, confirmedRequests>
            Map<Long, Long> views // <eventId, views>
    ) {
        List<EventFullDto> result = new ArrayList<>();
        for (Event e : event) {
            result.add(
                    eventToFullDto(
                        e,
                        initiators.get(e.getInitiator()),
                        confirmedRequests.getOrDefault(e.getId(), 0L),
                        views.getOrDefault(e.getId(), 0L)
                    )
            );
        }

        return result;
    }

    public EventShortDto eventToShortDto(
            Event event,
            UserShortDto initiator,
            Long confirmedRequests,
            Long views
    ) {
        return EventShortDto.builder()
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .eventDate(event.getEventDate().format(Constants.FORMATTER))
                .id(event.getId())
                .initiator(initiator)
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public List<EventShortDto> eventToShortDto(
            List<Event> event,
            Map<Long, UserShortDto> initiators,
            Map<Long, Long> confirmedRequests, // <eventId, confirmedRequests>
            Map<Long, Long> views // <eventId, views>
    ) {

        List<EventShortDto> result = new ArrayList<>();
        for (Event e : event) {
            result.add(
                    eventToShortDto(
                            e,
                            initiators.get(e.getInitiator()),
                            confirmedRequests.getOrDefault(e.getId(), 0L),
                            views.getOrDefault(e.getId(), 0L)
                    )
            );
        }

        return result;
    }

}