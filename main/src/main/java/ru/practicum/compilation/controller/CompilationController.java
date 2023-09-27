package ru.practicum.compilation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationDto;
import ru.practicum.compilation.service.CompilationService;

import javax.validation.Valid;
import java.util.List;

@RestController
@Validated
public class CompilationController {
    private final CompilationService compilationService;

    public CompilationController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @PostMapping("/admin/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto create(@Valid @RequestBody NewCompilationDto newCompilationDto) {

        return compilationService.create(newCompilationDto);
    }

    @PatchMapping("/admin/compilations/{compId}")
    public CompilationDto update(@Valid @RequestBody UpdateCompilationDto updateCompilationDto,
                                 @PathVariable Long compId) {

        return compilationService.update(compId, updateCompilationDto);
    }

    @DeleteMapping("/admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long compId) {

        compilationService.delete(compId);
    }

    @GetMapping("/compilations")
    public List<CompilationDto> getAll(@RequestParam(name = "pinned", required = false) Boolean pinned,
                                       @RequestParam(name = "from", required = false, defaultValue = "0") Integer from,
                                       @RequestParam(name = "size", required = false, defaultValue = "10") Integer size) {

        return compilationService.getAll(pinned, from, size);
    }

    @GetMapping("/compilations/{compId}")
    public CompilationDto getById(@PathVariable Long compId) {

        return compilationService.getById(compId);
    }
}
