package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationDto;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto newCompilationDto) {

        Compilation compilation = compilationMapper.toCompilation(newCompilationDto);

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            Set<Long> eventIds = newCompilationDto.getEvents();
            Set<Event> events = new HashSet<>(eventRepository.findAllByIdIn(eventIds));
            compilation.setEvents(events);
        } else {
            compilation.setEvents(new HashSet<>());
        }

        Compilation compilationToSave = compilationRepository.save(compilation);
        return compilationMapper.toCompilationDto(compilationToSave);
    }

    @Override
    @Transactional
    public CompilationDto update(Long compilationId, UpdateCompilationDto updateCompilationDto) {

        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Подборка событий с ID [" + compilationId + "] не найдена."));

        if (updateCompilationDto.getEvents() != null && !updateCompilationDto.getEvents().isEmpty()) {
            Set<Long> eventIds = updateCompilationDto.getEvents();
            Set<Event> events = new HashSet<>(eventRepository.findAllByIdIn(eventIds));
            compilation.setEvents(events);
        }
        if (updateCompilationDto.getPinned() != null) {
            compilation.setPinned(updateCompilationDto.getPinned());
        }
        if (updateCompilationDto.getTitle() != null) {
            compilation.setTitle(updateCompilationDto.getTitle());
        }
        Compilation updated = compilationRepository.save(compilation);
        return compilationMapper.toCompilationDto(updated);
    }

    @Override
    @Transactional
    public void delete(Long compilationId) {

        compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Подборка событий с ID [" + compilationId + "] не найдена."));

        compilationRepository.deleteById(compilationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getAll(Boolean pinned, Integer from, Integer size) {

        Page<Compilation> compilationEntities = compilationRepository.findAllByPinned(pinned,
                PageRequest.of(from / size, size, Sort.by("id")));

        return compilationEntities
                .stream()
                .map(compilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getById(Long compilationId) {

        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Подборка событий с ID [" + compilationId + "] не найдена."));

        return compilationMapper.toCompilationDto(compilation);
    }
}
