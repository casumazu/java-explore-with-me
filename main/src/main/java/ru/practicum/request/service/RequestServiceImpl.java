package ru.practicum.request.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.*;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.model.RequestStatusToUpdate;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    @Override
    public RequestDto create(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено."));

        Request request = new Request(LocalDateTime.now(), event, user, RequestStatus.PENDING);
        Optional<Request> requests = requestRepository.findByRequesterIdAndEventId(userId, eventId);

        checkExceptionsForCreateRequest(userId, requests, event, request);

        if (!event.getRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        }
        Request requestToSave = requestRepository.save(request);

        return requestMapper.toRequestDto(requestToSave);
    }

    private void checkExceptionsForCreateRequest(Long userId, Optional<Request> requests, Event event, Request request) {
        if (event.getInitiator().getId().equals(userId)) {
            throw new NotAvailableException("Ошибка. Событие организовано пользователем.");
        }
        if (requests.isPresent()) {
            throw new NotAvailableException("Ошибка. Запрос был отправлен ранее.");
        }
        if (!(event.getState().equals(State.PUBLISHED))) {
            throw new NotAvailableException("Ошибка. Событие не опубликовано.");
        }
        int limit = event.getParticipantLimit();
        if (limit != 0) {
            if (limit == event.getConfirmedRequests()) {
                throw new ExistsException("Ошибка. Достигнут лимит запросов на участие.");
            }
        } else {
            request.setStatus(RequestStatus.CONFIRMED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestDto> getCurrentUserRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID {" + userId + "} не найден."));

        List<Request> requests = requestRepository.findAllByRequesterIdInForeignEvents(userId);

        return requests.stream()
                .map(requestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public RequestDto cancel(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID [" + userId + "] не найден."));

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявка с ID [" + requestId + "] не найдена."));

        if (!request.getRequester().getId().equals(userId)) {
            throw new ValidationException("Ошибка. Заявка не принадлежит пользователю.");
        }
        if (request.getStatus().equals(RequestStatus.REJECTED) || request.getStatus().equals(RequestStatus.CANCELED)) {
            throw new BadRequestException("Ошибка. Заявка была отклонена ранее.");
        }
        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
            throw new BadRequestException("Ошибка. Нельзя отменить подтвержденную заявку.");
        }
        request.setStatus(RequestStatus.CANCELED);

        return requestMapper.toRequestDto(requestRepository.save(request));
    }

    @Transactional(readOnly = true)
    @Override
    public List<RequestDto> getRequestsByUserOfEvent(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID [" + userId + "] не найден."));

        eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID [" + eventId + "] не найдено."));

        List<Request> requests = requestRepository.findAllUserRequestsInEvent(userId, eventId);

        return requests.stream()
                .map(requestMapper::toRequestDto)
                .collect(Collectors.toList());
    }


    @Override
    public EventRequestStatusUpdateResult updateRequests(Long userId, Long eventId,
                                                         EventRequestStatusUpdateRequest eventRequest) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID [" + userId + "] не найден."));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID [" + eventId + "] не найдено."));

        if (event.getParticipantLimit() == 0 && !event.getRequestModeration()) {
            throw new ExistsException("Подтверждение заявки не требуется.");
        }
        if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ExistsException("Превышен лимит подтвержденных заявок.");
        }

        List<Long> requestIds = eventRequest.getRequestIds();
        RequestStatusToUpdate status = eventRequest.getStatus();

        List<Request> requests = requestIds.stream()
                .map(id -> requestRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Заявка с ID [" + id + "] не найдена.")))
                .collect(Collectors.toList());

        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();
        List<Request> updatedRequests = new ArrayList<>();

        for (Request req : requests) {
            if (status == RequestStatusToUpdate.CONFIRMED && req.getStatus().equals(RequestStatus.PENDING)) {
                if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
                    req.setStatus(RequestStatus.REJECTED);
                    updatedRequests.add(req);
                    rejectedRequests.add(req);
                }
                req.setStatus(RequestStatus.CONFIRMED);
                updatedRequests.add(req);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                confirmedRequests.add(req);
            }
            if (status == RequestStatusToUpdate.REJECTED && req.getStatus().equals(RequestStatus.PENDING)) {
                req.setStatus(RequestStatus.REJECTED);
                updatedRequests.add(req);
                rejectedRequests.add(req);
            }
        }

        requestRepository.saveAll(updatedRequests);
        eventRepository.save(event);

        List<RequestDto> confirmedRequestDtos = confirmedRequests.stream()
                .map(requestMapper::toRequestDto)
                .collect(Collectors.toList());

        List<RequestDto> rejectedRequestDtos = rejectedRequests.stream()
                .map(requestMapper::toRequestDto)
                .collect(Collectors.toList());

        EventRequestStatusUpdateResult updateResult = new EventRequestStatusUpdateResult();
        updateResult.setRejectedRequests(rejectedRequestDtos);
        updateResult.setConfirmedRequests(confirmedRequestDtos);

        return updateResult;
    }
}
