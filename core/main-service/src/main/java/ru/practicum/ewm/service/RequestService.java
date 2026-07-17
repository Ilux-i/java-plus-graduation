package ru.practicum.ewm.service;


import ru.practicum.request.dto.CreateUpdateRequestDto;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    //    Создание нового запроса
    ParticipationRequestDto createRequest(CreateUpdateRequestDto dto);

    //    Получение всех запросов определённого пользователя
    List<ParticipationRequestDto> getRequestByUserId(Long userId);

    //    Отмена запроса на событие
    ParticipationRequestDto canceledRequest(Long userId, Long requestId);

}
