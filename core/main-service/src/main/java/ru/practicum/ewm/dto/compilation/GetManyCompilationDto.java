package ru.practicum.ewm.dto.compilation;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetManyCompilationDto {

    private Boolean pinned;
    private Integer from;
    private Integer size;

}
