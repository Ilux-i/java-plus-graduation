package ru.practicum.event.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.event.dto.compilation.CompilationDto;
import ru.practicum.event.dto.compilation.CreateCompilationDto;
import ru.practicum.event.dto.compilation.UpdateCompilationDto;
import ru.practicum.event.dto.event.EventShortDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Compilation;

import java.util.List;

@UtilityClass
public class CompilationMapper {

    //Преобразование в сущность
    public Compilation toEntity(CreateCompilationDto dto, List<Event> events) {
        return Compilation.builder()
                .events(events)
                .pinned(dto.getPinned())
                .title(dto.getTitle())
                .build();
    }

    //Преобразование в dto
    public CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> events) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .events(events)
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }

    public UpdateCompilationDto toUpdateCompilationDto(Long compId, CreateCompilationDto dto) {
        return UpdateCompilationDto.builder()
                .id(compId)
                .events(dto.getEvents())
                .pinned(dto.getPinned())
                .title(dto.getTitle())
                .build();
    }

}
