package ru.practicum.ewm.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.compilation.CompilationDto;
import ru.practicum.event.dto.compilation.GetManyCompilationDto;
import ru.practicum.ewm.service.CompilationService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/compilations")
public class CompilationController {

    private final CompilationService service;

    @GetMapping
    public List<CompilationDto> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size
    ) {
        return service.getCompilations(GetManyCompilationDto.builder()
                .pinned(pinned)
                .from(from)
                .size(size)
                .build()
        );
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(
            @PositiveOrZero @PathVariable Long compId
    ) {
        return service.getCompilationById(compId);
    }

}