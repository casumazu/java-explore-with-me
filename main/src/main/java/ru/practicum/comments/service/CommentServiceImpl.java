package ru.practicum.comments.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.mapper.CommentMapper;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;

    @Override
    public CommentDto createComment(Long userId, CommentDto commentDto, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID {" + userId + "} найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID {" + eventId + "} не найдено"));
        Optional<Request> request = requestRepository.findByRequesterIdAndEventId(userId, eventId);

        if (request.isEmpty()) {
            throw new NotFoundException("Ошибка. Пользователь с ID{" + userId + "} не участвовал" +
                    " в событии ID{" + eventId + "}");
        }
        Comment comment = commentMapper.toComment(commentDto);
        comment.setAuthor(user);
        comment.setEvent(event);

        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto updateComment(Long commentId, CommentDto commentDto) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с ID{" + commentId + "} не найден"));

        comment.setText(commentDto.getText());
        comment.setCreated(commentDto.getCreated());

        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID {" + userId + "} найден"));
        commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с ID {" + commentId + "} не найден"));
        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentDto> getAllByEventId(Long eventId) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID {" + eventId + "} не найдено"));
        return commentRepository.findAllByEventId(eventId)
                .stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getAllByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID {" + userId + "} не найден"));
        return commentRepository.findAllByAuthorId(userId)
                .stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
    }
}