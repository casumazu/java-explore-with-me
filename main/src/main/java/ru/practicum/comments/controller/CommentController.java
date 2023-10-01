package ru.practicum.comments.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.service.CommentService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/users/{userId}/events/{eventId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable Long eventId, @Valid @RequestBody CommentDto commentDto,
                                    @PathVariable Long userId) {
        return commentService.createComment(userId, commentDto, eventId);
    }

    @DeleteMapping("/admin/comments/{userId}/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId, @PathVariable Long commentId) {
        commentService.deleteComment(userId, commentId);
    }

    @PatchMapping("/admin/comments/{commentId}")
    public CommentDto updateComment(@PathVariable Long commentId, @Valid @RequestBody CommentDto commentDto) {
        return commentService.updateComment(commentId, commentDto);
    }

    @GetMapping("/events/{eventId}/comments/all")
    public List<CommentDto> getAllEventsComments(@PathVariable Long eventId) {
        return commentService.getAllByEventId(eventId);
    }

    @GetMapping("/events/comments/all/{userId}")
    public List<CommentDto> getAllUserComments(@PathVariable Long userId) {
        return commentService.getAllByUserId(userId);
    }
}