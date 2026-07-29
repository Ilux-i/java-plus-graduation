package ru.practicum.event.dto.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmedRequestCount {
    Long eventId;
    Long count;
}