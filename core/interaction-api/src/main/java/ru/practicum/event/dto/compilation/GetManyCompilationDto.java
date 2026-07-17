package ru.practicum.event.dto.compilation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetManyCompilationDto {

    private Boolean pinned;
    private Integer from;
    private Integer size;

}
