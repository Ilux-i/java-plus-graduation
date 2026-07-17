package ru.practicum.request.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class CreateUpdateRequestDto {

    @NotNull
    @PositiveOrZero
    Long userId;

    @NotNull
    @PositiveOrZero
    Long eventId;

}
