package ru.practicum.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.event.dto.event.ConfirmedRequestCount;
import ru.practicum.repository.RequestRepository;
import ru.practicum.request.ParticipationRequest;
import ru.practicum.request.RequestStatus;

import java.util.List;

@Service
@AllArgsConstructor
public class RequestInteriorService {

    private final RequestRepository requestRepository;

    public List<ParticipationRequest> findAllByEventId(Long eventId) {
        return requestRepository.findAllByEvent(eventId);
    }

    public Long countByEventIdAndStatus(Long eventId, RequestStatus requestStates) {
        return requestRepository.countByEventAndStatus(eventId, requestStates);
    }

    public List<ParticipationRequest> findAllById(List<Long> requestIds) {
        return requestRepository.findAllById(requestIds);
    }

    public void saveAll(List<ParticipationRequest> requests) {
        requestRepository.saveAll(requests);
    }

    public List<ConfirmedRequestCount> countConfirmedRequestsByEventIds(List<Long> eventIds, RequestStatus requestStatus) {
        return requestRepository.countConfirmedRequestsByEventIds(eventIds, requestStatus);
    }

}
