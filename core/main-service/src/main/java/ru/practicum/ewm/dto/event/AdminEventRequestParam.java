package ru.practicum.ewm.dto.event;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class AdminEventRequestParam {
    List<@Positive Long> users;
    List<String> states;
    List<@Positive Long> categories;
    String rangeStart;
    String rangeEnd;

    @PositiveOrZero
    Integer from;

    @Positive
    Integer size;
}