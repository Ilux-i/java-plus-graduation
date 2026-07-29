package ru.practicum.service.event;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.event.model.Event;
import ru.practicum.repository.event.EventRepository;

@Service
@RequiredArgsConstructor
public class EventInteriorService {

    private final EventRepository repository;

    public Event findById(Long eventId) {
        return repository.findById(eventId).orElseThrow(
                () -> new NotFoundException("События с id: " + eventId + ", не найдено.")
        );
    }

    public Boolean existsById(Long eventId) {
        return repository.existsById(eventId);
    }

}
