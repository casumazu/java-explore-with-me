package ru.practicum.comments.service;

import ru.practicum.comments.dto.CommentDto;

import java.util.List;

public interface CommentService {
    CommentDto createComment(Long userId, CommentDto commentDto, Long eventId);

    CommentDto updateComment(Long commentId, CommentDto commentDto);

    void deleteComment(Long userId, Long commentId);

    List<CommentDto> getAllByEventId(Long eventId);

    List<CommentDto> getAllByUserId(Long userId);
}