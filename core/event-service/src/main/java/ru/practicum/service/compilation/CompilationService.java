package ru.practicum.service.compilation;


import ru.practicum.event.dto.compilation.CompilationDto;
import ru.practicum.event.dto.compilation.CreateCompilationDto;
import ru.practicum.event.dto.compilation.GetManyCompilationDto;
import ru.practicum.event.dto.compilation.UpdateCompilationDto;

import java.util.List;

public interface CompilationService {
    CompilationDto createCompilation(CreateCompilationDto dto);

    CompilationDto updateCompilation(UpdateCompilationDto dto);

    void removeCompilation(Long compId);

    List<CompilationDto> getCompilations(GetManyCompilationDto dto);

    CompilationDto getCompilationById(Long compId);
}
