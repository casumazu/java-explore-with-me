package ru.practicum.exception;

public class ExistsException extends RuntimeException {
    public ExistsException(String message) {
        super(message);
    }
}