package ru.practicum.ewm.repository;

import org.springframework.data.domain.Pageable;
import ru.practicum.event.dto.event.AdminEventRequestParam;
import ru.practicum.event.dto.event.PublicEventRequestParam;
import ru.practicum.event.model.Event;

import java.util.List;

public interface CustomEventRepository {

    List<Event> findByAdminRequest(AdminEventRequestParam param, Pageable pageable);

    List<Event> findByPublicRequest(PublicEventRequestParam param, Pageable pageable);

}