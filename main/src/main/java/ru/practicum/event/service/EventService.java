package ru.practicum.event.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Sort;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventService {

    EventDto create(Long userId, NewEventDto newEventDto);

    EventDto update(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventDto updateAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventShortDto> getAllPublic(String text, List<Long> categoriesIds, Boolean paid, String rangeStart,
                                     String rangeEnd, Boolean onlyAvailable, Sort sort, Integer from,
                                     Integer size, HttpServletRequest request);

    List<EventDto> getAllAdmin(List<Long> userIds, List<String> states, List<Long> categories,
                               String rangeStart, String rangeEnd, Integer from, Integer size,
                               HttpServletRequest request);

    List<EventShortDto> getAllByUser(Long userId, Pageable pageable);

    EventDto getById(Long userId, Long eventId);

    EventDto getByIdPublic(Long eventId, HttpServletRequest httpRequest);
}
