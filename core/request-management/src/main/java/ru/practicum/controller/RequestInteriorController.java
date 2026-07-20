package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.event.ConfirmedRequestCount;
import ru.practicum.request.ParticipationRequest;
import ru.practicum.request.RequestStatus;
import ru.practicum.service.RequestInteriorService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/interior/requests")
public class RequestInteriorController {

    private final RequestInteriorService service;

    @GetMapping("/{eventId}")
    public List<ParticipationRequest> findAllByEvent_Id(
            @PathVariable Long eventId
    ) {
        return service.findAllByEventId(eventId);
    }

    @GetMapping("/{eventId}/count")
    public Long countByEvent_IdAndStatus(
            @PathVariable Long eventId,
            @RequestParam RequestStatus requestStates
    ) {
        return service.countByEventIdAndStatus(eventId, requestStates);
    }

    @GetMapping("/all")
    public List<ParticipationRequest> findAllById(
            @RequestParam List<Long> requestIds
    ) {
        return service.findAllById(requestIds);
    }

    @PostMapping("/save/all")
    public void saveAll(
            @RequestBody List<ParticipationRequest> requests
    ) {
        service.saveAll(requests);
    }

    @GetMapping("/confirmed/all")
    public List<ConfirmedRequestCount> countConfirmedRequestsByEventIds(
            @RequestBody List<Long> eventIds,
            @RequestParam RequestStatus requestStatus
    ) {
        return service.countConfirmedRequestsByEventIds(eventIds, requestStatus);
    }

}