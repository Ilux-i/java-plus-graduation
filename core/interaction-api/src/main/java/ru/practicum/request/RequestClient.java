package ru.practicum.request;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.event.ConfirmedRequestCount;

import java.util.List;

@FeignClient(name = "REQUEST-MANAGEMENT", path = "/interior/requests")
public interface RequestClient {

    @GetMapping("/{eventId}")
    List<ParticipationRequest> findAllByEventId(
            @PathVariable Long eventId
    );

    @GetMapping("/{eventId}/count")
    Long countByEventIdAndStatus(
            @PathVariable Long eventId,
            @RequestParam RequestStatus requestStates
    );

    @GetMapping("/all")
    List<ParticipationRequest> findAllById(
            @RequestParam List<Long> requestIds
    );

    @PostMapping("/save/all")
    void saveAll(
            @RequestBody List<ParticipationRequest> requests
    );

    @PostMapping("/confirmed/all")
    List<ConfirmedRequestCount> countConfirmedRequestsByEventIds(
            @RequestBody List<Long> eventIds,
            @RequestParam RequestStatus requestStatus
    );
}
