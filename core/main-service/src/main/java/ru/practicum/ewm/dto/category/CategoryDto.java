package ru.practicum.ewm.dto.category;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    Long id;
    @Size(max = 50, message = "Длина имени категории должна быть не более 50 символов")
    String name;
}