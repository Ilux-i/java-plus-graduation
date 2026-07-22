package ru.practicum.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.event.model.Event;

@FeignClient(name = "EVENT-MANAGEMENT", path = "/interior/events")
public interface EventClient {

    @GetMapping("/{eventId}")
    Event findById(
            @PathVariable Long eventId
    );

    @GetMapping("/{eventId}/exists")
    Boolean existsById(
            @PathVariable Long eventId
    );

}
