package ru.practicum.comments.mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.model.Comment;

@Component
@Mapper(componentModel = "spring")
public interface CommentMapper {
    Comment toComment(CommentDto commentDto);
    CommentDto toCommentDto(Comment comment);
}