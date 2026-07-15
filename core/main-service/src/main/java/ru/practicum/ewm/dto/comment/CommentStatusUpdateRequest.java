package ru.practicum.ewm.dto.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentStatusUpdateRequest {
    @NotBlank(message = "Статус обязателен")
    private String status;
}