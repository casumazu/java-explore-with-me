package ru.practicum.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RequestDto {
    public static final String DATE = "yyyy-MM-dd HH:mm:ss";

    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE)
    private LocalDateTime created;

    private Long event;

    private Long requester;

    private String status;
}
