package ru.practicum.event.dto.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.annotation.NotBlankOrNull;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventUserRequest {

    @NotBlankOrNull
    @Size(min = 20, max = 2000, message = "Длина аннотации должна быть не менее 20 и не более 2000 символов")
    String annotation;
    Long category;

    @NotBlankOrNull
    @Size(min = 20, max = 7000, message = "Длина описания события должна быть не менее 20 и не более 7000 символов")
    String description;

    @NotBlankOrNull
    String eventDate;

    @Valid
    LocationDto location;
    Boolean paid;
    @PositiveOrZero
    Integer participantLimit;
    Boolean requestModeration;

    @NotBlankOrNull
    String stateAction;

    @NotBlankOrNull
    @Size(min = 3, max = 120, message = "Длина заголовка события должна быть не менее 3 и не более 120 символов")
    String title;
}