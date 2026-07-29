package ru.practicum.event.dto.event;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.dto.category.CategoryDto;
import ru.practicum.user.dto.UserShortDto;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class EventShortDto {
    String annotation;
    CategoryDto category;
    Long confirmedRequests;
    String eventDate;
    Long id;
    UserShortDto initiator;
    Boolean paid;
    String title;
    Long views;
}