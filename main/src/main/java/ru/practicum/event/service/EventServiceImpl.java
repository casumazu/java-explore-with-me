package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;

import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.*;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.exception.*;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.praktikum.stats.client.StatisticsClient;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final CategoryRepository categoryRepository;
    private final StatisticsClient statsClient = new StatisticsClient("http://stat-server:9090", new RestTemplateBuilder());

    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public EventDto create(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        if (newEventDto.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала события должна быть не ранее чем за час от даты публикации.");
        }

        Event eventToSave = eventMapper.toEventModel(newEventDto);
        eventToSave.setState(State.PENDING);
        eventToSave.setConfirmedRequests(0L);
        eventToSave.setCreatedOn(LocalDateTime.now());

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с ID " + newEventDto.getCategory() + " не найдена."));

        eventToSave.setCategory(category);
        eventToSave.setInitiator(user);

        return eventMapper.toEventDto(eventRepository.save(eventToSave));
    }

    @Override
    @Transactional
    public EventDto update(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено."));

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        if (event.getState().equals(State.CANCELED) || event.getState().equals(State.PENDING)) {
            if (updateEventUserRequest.getEventDate() != null && updateEventUserRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException("Дата и время события не могут быть менее " +
                        "чем через два часа от текущего момента.");
            }

            if (StateActionUser.SEND_TO_REVIEW == updateEventUserRequest.getStateAction()) {
                event.setState(State.PENDING);
            }
            if (StateActionUser.CANCEL_REVIEW == updateEventUserRequest.getStateAction()) {
                event.setState(State.CANCELED);
            }
        } else {
            throw new ExistsException("Изменить можно только отмененные события или события в состоянии ожидания модерации, " +
                    "статус события: " + event.getState());
        }

        updateEventByUser(updateEventUserRequest, event);
        eventRepository.save(event);

        return eventMapper.toEventDto(event);
    }

    @Override
    public EventDto updateAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {

        Event eventToUpdate = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено."));

        if ((updateEventAdminRequest.getEventDate() != null)
                && (updateEventAdminRequest.getEventDate().isBefore(LocalDateTime.now()))) {
            throw new ValidationException("WARNING. " +
                    "Дата начала события должна быть не ранее чем за час от даты публикации.");
        }

        if (updateEventAdminRequest.getStateAction() != null) {
            if (updateEventAdminRequest.getStateAction() == StateActionAdmin.PUBLISH_EVENT) {
                if (eventToUpdate.getState().equals(State.PENDING)) {
                    eventToUpdate.setState(State.PUBLISHED);
                    eventToUpdate.setPublishedOn(LocalDateTime.now());
                } else {
                    throw new ExistsException("WARNING. " +
                            "Событие можно публиковать, только если оно в состоянии ожидания публикации.");
                }
            }
            if (updateEventAdminRequest.getStateAction() == StateActionAdmin.REJECT_EVENT) {
                if (eventToUpdate.getState().equals(State.PUBLISHED)) {
                    throw new ExistsException("WARNING. " +
                            "Событие невозможно отклонить, если оно уже опубликовано.");
                }
                eventToUpdate.setState(State.CANCELED);
            }
        }

        updateEventByAdmin(updateEventAdminRequest, eventToUpdate);
        eventRepository.save(eventToUpdate);

        return eventMapper.toEventDto(eventToUpdate);
    }

    @Override
    public List<EventShortDto> getAllPublic(String text, List<Long> categoriesIds, Boolean paid, String rangeStart,
                                            String rangeEnd, Boolean onlyAvailable, Sort sort, Integer from,
                                            Integer size, HttpServletRequest request) {

        LocalDateTime start = null;
        LocalDateTime end = null;

        if (rangeStart != null && rangeEnd != null) {
            start = LocalDateTime.parse(rangeStart, dateFormatter);
            end = LocalDateTime.parse(rangeEnd, dateFormatter);
            if (start.isAfter(end)) {
                throw new ValidationException("Даты некорректны.");
            }
        } else {
            if (rangeStart == null && rangeEnd == null) {
                start = LocalDateTime.now();
                end = LocalDateTime.now().plusYears(10);
            } else {
                if (rangeStart == null) {
                    start = LocalDateTime.now();
                }
                if (rangeEnd == null) {
                    end = LocalDateTime.now();
                }
            }
        }

        final PageRequest pageRequest = PageRequest.of(from / size, size,
                org.springframework.data.domain.Sort.by(Sort.EVENT_DATE.equals(sort) ? "eventDate" : "views"));

        List<Event> eventEntities = eventRepository.searchPublishedEvents(categoriesIds, paid, start, end, pageRequest)
                .getContent();

        statsClient.saveHit("ewm-service", request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now());

        if (eventEntities.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> eventIds = getEventIds(text, eventEntities);

        Map<Long, Long> statsMap = statsClient.getSetViewsByEventId(eventIds);

        List<EventShortDto> events = eventEntities
                .stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());

        events.forEach(eventShortDto ->
                eventShortDto.setViews(statsMap.getOrDefault(eventShortDto.getId(), 0L)));

        return events;
    }

    private Set<Long> getEventIds(String text, List<Event> eventEntities) {
        java.util.function.Predicate<Event> eventEntityPredicate;
        if (text != null && !text.isEmpty()) {
            eventEntityPredicate = eventEntity -> eventEntity.getAnnotation().toLowerCase().contains(text.toLowerCase())
                    || eventEntity.getDescription().toLowerCase().contains(text.toLowerCase());
        } else {
            eventEntityPredicate = eventEntity -> true;
        }

        return eventEntities.stream()
                .filter(eventEntityPredicate)
                .map(Event::getId)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getAllByUser(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        return eventMapper.toEventShortDtoList(eventRepository.findAllByInitiatorId(userId, pageable).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDto> getAllAdmin(List<Long> userIds, List<String> states, List<Long> categories,
                                      String rangeStart, String rangeEnd, Integer from, Integer size,
                                      HttpServletRequest request) {

        final PageRequest pageRequest = PageRequest.of(from / size, size);

        if (states == null && rangeStart == null && rangeEnd == null) {
            return eventRepository.findAll(pageRequest)
                    .stream()
                    .map(eventMapper::toEventDto)
                    .collect(Collectors.toList());
        }

        List<State> stateList = states
                .stream()
                .map(State::valueOf)
                .collect(Collectors.toList());

        LocalDateTime start;
        if (rangeStart != null && !rangeStart.isEmpty()) {
            start = LocalDateTime.parse(rangeStart, dateFormatter);
        } else {
            start = LocalDateTime.now().plusYears(5);
        }

        LocalDateTime end;
        if (rangeEnd != null && !rangeEnd.isEmpty()) {
            end = LocalDateTime.parse(rangeEnd, dateFormatter);
        } else {
            end = LocalDateTime.now().plusYears(5);
        }

        if (!userIds.isEmpty() && !states.isEmpty() && !categories.isEmpty()) {
            return getEventDtoListWithAllParameters(userIds, categories, pageRequest, stateList, start, end);
        }
        if (userIds.isEmpty() && !categories.isEmpty()) {
            return getEventDtoListWithAllParameters(userIds, categories, pageRequest, stateList, start, end);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EventDto getById(Long userId, Long eventId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));
        eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено."));

        return eventMapper.toEventDto(eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с заданными параметрами не существует.")));
    }

    @Override
    @Transactional(readOnly = true)
    public EventDto getByIdPublic(Long eventId, HttpServletRequest httpRequest) {

        Event event = eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено."));

        statsClient.saveHit(
                "ewm-service",
                httpRequest.getRequestURI(),
                httpRequest.getRemoteAddr(),
                LocalDateTime.now());

        Long views = statsClient.getStatisticsByEventId(eventId);

        EventDto eventDto = eventMapper.toEventDto(event);
        eventDto.setViews(views);

        return eventDto;
    }

    private void updateEventByAdmin(UpdateEventAdminRequest event, Event eventToUpdate) {

        eventToUpdate.setAnnotation(Objects.requireNonNullElse(event.getAnnotation(), eventToUpdate.getAnnotation()));

        eventToUpdate.setCategory(event.getCategory() == null
                ? eventToUpdate.getCategory()
                : categoryRepository.findById(event.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с ID " + event.getCategory() + " не найдена.")));

        eventToUpdate.setDescription(Objects.requireNonNullElse(event.getDescription(),
                eventToUpdate.getDescription()));

        eventToUpdate.setEventDate(Objects.requireNonNullElse(event.getEventDate(),
                eventToUpdate.getEventDate()));

        eventToUpdate.setLocation(event.getLocation() == null
                ? eventToUpdate.getLocation()
                : locationRepository.findByLatAndLon(event.getLocation().getLat(), event.getLocation().getLon())
                .orElse(new Location(null, event.getLocation().getLat(), event.getLocation().getLon())));

        eventToUpdate.setPaid(Objects.requireNonNullElse(event.getPaid(), eventToUpdate.getPaid()));
        eventToUpdate.setParticipantLimit(Objects.requireNonNullElse(event.getParticipantLimit(),
                eventToUpdate.getParticipantLimit()));
        eventToUpdate.setRequestModeration(Objects.requireNonNullElse(event.getRequestModeration(),
                eventToUpdate.getRequestModeration()));
        eventToUpdate.setTitle(Objects.requireNonNullElse(event.getTitle(), eventToUpdate.getTitle()));
    }

    private void updateEventByUser(UpdateEventUserRequest event, Event eventToUpdate) {

        eventToUpdate.setAnnotation(Objects.requireNonNullElse(event.getAnnotation(), eventToUpdate.getAnnotation()));

        eventToUpdate.setCategory(event.getCategory() == null
                ? eventToUpdate.getCategory()
                : categoryRepository.findById(event.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с ID " + event.getCategory() + " не найдена.")));

        eventToUpdate.setDescription(Objects.requireNonNullElse(event.getDescription(),
                eventToUpdate.getDescription()));

        eventToUpdate.setEventDate(Objects.requireNonNullElse(event.getEventDate(),
                eventToUpdate.getEventDate()));

        eventToUpdate.setLocation(event.getLocation() == null
                ? eventToUpdate.getLocation()
                : locationRepository.findByLatAndLon(event.getLocation().getLat(), event.getLocation().getLon())
                .orElse(new Location(null, event.getLocation().getLat(), event.getLocation().getLon())));

        eventToUpdate.setPaid(Objects.requireNonNullElse(event.getPaid(), eventToUpdate.getPaid()));
        eventToUpdate.setParticipantLimit(Objects.requireNonNullElse(event.getParticipantLimit(),
                eventToUpdate.getParticipantLimit()));
        eventToUpdate.setRequestModeration(Objects.requireNonNullElse(event.getRequestModeration(),
                eventToUpdate.getRequestModeration()));
        eventToUpdate.setTitle(Objects.requireNonNullElse(event.getTitle(), eventToUpdate.getTitle()));
    }

    private List<EventDto> getEventDtoListWithAllParameters(List<Long> userIds, List<Long> categories,
                                                            PageRequest pageRequest, List<State> stateList,
                                                            LocalDateTime start, LocalDateTime end) {

        Page<Event> eventsWithPage = eventRepository.findAllWithAllParameters(userIds, stateList,
                categories, start, end,
                pageRequest);

        Set<Long> eventIds = eventsWithPage
                .stream()
                .map(Event::getId)
                .collect(Collectors.toSet());

        Map<Long, Long> viewStatsMap = statsClient.getSetViewsByEventId(eventIds);

        List<EventDto> events = eventsWithPage
                .stream()
                .map(eventMapper::toEventDto)
                .collect(Collectors.toList());

        events.forEach(eventFullDto ->
                eventFullDto.setViews(viewStatsMap.getOrDefault(eventFullDto.getId(), 0L)));

        return events;
    }
}