package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Sort;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.service.RequestService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class EventController {
    private final EventService eventService;
    private final RequestService requestService;

    @PatchMapping("/admin/events/{eventId}")
    public EventDto updateAdmin(@PathVariable(name = "eventId") Long eventId,
                                @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {

        return eventService.updateAdmin(eventId, updateEventAdminRequest);
    }

    @GetMapping("/admin/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventDto> getAllAdmin(@RequestParam(name = "users", required = false) List<Long> userIds,
                                      @RequestParam(name = "states", required = false) List<String> states,
                                      @RequestParam(name = "categories", required = false) List<Long> categories,
                                      @RequestParam(name = "rangeStart", required = false) String rangeStart,
                                      @RequestParam(name = "rangeEnd", required = false) String rangeEnd,
                                      @RequestParam(name = "from", defaultValue = "0") Integer from,
                                      @RequestParam(name = "size", defaultValue = "10") Integer size,
                                      HttpServletRequest request) {

        return eventService.getAllAdmin(userIds, states, categories, rangeStart, rangeEnd, from, size, request);
    }

    @GetMapping("/events")
    public List<EventShortDto> getAllPublic(@RequestParam(required = false) String text,
                                            @RequestParam(required = false) List<Long> categories,
                                            @RequestParam(required = false) Boolean paid,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String rangeStart,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String rangeEnd,
                                            @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
                                            @RequestParam(required = false) Sort sort,
                                            @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
                                            @RequestParam(required = false, defaultValue = "10") @Positive Integer size,
                                            HttpServletRequest request) {

        return eventService.getAllPublic(text, categories, paid, rangeStart, rangeEnd, onlyAvailable,
                sort, from, size, request);
    }

    @GetMapping("/events/{id}")
    public EventDto getByIdPublic(@PathVariable Long id, HttpServletRequest httpRequest) {

        return eventService.getByIdPublic(id, httpRequest);
    }

    @GetMapping("/users/{userId}/events")
    public List<EventShortDto> getAllByUser(@PathVariable Long userId,
                                            @RequestParam(name = "from", defaultValue = "0", required = false) Integer from,
                                            @RequestParam(name = "size", defaultValue = "10", required = false) Integer size) {

        return eventService.getAllByUser(userId, PageRequest.of(from, size));
    }

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto create(@PathVariable Long userId, @Valid @RequestBody NewEventDto newEventDto) {

        return eventService.create(userId, newEventDto);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventDto getById(@PathVariable Long userId, @PathVariable Long eventId) {

        return eventService.getById(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventDto update(@PathVariable Long userId, @PathVariable Long eventId,
                           @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {

        return eventService.update(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<RequestDto> getAllRequests(@PathVariable Long userId, @PathVariable Long eventId) {

        return requestService.getRequestsByUserOfEvent(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequests(@PathVariable Long userId,
                                                         @PathVariable Long eventId,
                                                         @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {

        return requestService.updateRequests(userId, eventId, eventRequestStatusUpdateRequest);
    }
}