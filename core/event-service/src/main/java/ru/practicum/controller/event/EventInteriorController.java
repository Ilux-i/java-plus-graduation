package ru.practicum.controller.event;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.model.Event;
import ru.practicum.service.event.EventInteriorService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/interior/events")
public class EventInteriorController {

    private final EventInteriorService service;

    @GetMapping("/{eventId}")
    public Event findById(
            @PathVariable @PositiveOrZero Long eventId
    ) {
        return service.findById(eventId);
    }

    @GetMapping("/{eventId}/exists")
    public Boolean existsById(
            @PathVariable @PositiveOrZero Long eventId
    ) {
        return service.existsById(eventId);
    }

}