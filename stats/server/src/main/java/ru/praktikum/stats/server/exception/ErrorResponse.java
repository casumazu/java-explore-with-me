package ru.praktikum.stats.server.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private String message;
    private String reason;
    private String status;
    private String timestamp;
}
