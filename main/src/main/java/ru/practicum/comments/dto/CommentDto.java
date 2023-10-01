package ru.practicum.comments.dto;


import lombok.Data;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.user.dto.UserDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Valid
public class CommentDto {
    private Long id;
    @NotNull
    @Size(min = 10, max = 7000)
    private String text;
    private UserDto author;
    private EventShortDto event;
    private LocalDateTime created;
}