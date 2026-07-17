package ru.practicum.request;

import lombok.experimental.UtilityClass;
import ru.practicum.constants.Constants;
import ru.practicum.event.model.Event;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.user.User;

import java.time.LocalDateTime;

@UtilityClass
public class RequestMapper {

    //Преобразование в сущность
    public ParticipationRequest toEntity(LocalDateTime nowData, Event event, User requester, RequestStatus status) {
        return ParticipationRequest.builder()
                .created(nowData)
                .event(event)
                .requester(requester)
                .status(status)
                .build();
    }

    //Преобразование в dto
    public ParticipationRequestDto toParticipationRequestDto(ParticipationRequest req) {
        return ParticipationRequestDto.builder()
                .id(req.getId())
                .created(req.getCreated().format(Constants.FORMATTER))
                .event(req.getEvent().getId())
                .requester(req.getRequester().getId())
                .status(req.getStatus().toString())
                .build();
    }

}