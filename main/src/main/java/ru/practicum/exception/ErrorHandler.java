package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ApiError handleNotFoundException(final NotFoundException e) {
        log.error("404 {} ", e.getMessage(), e);

        return new ApiError(e.getMessage(), "NotFound",
                HttpStatus.NOT_FOUND.getReasonPhrase().toUpperCase(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ApiError handleNotAvailableException(NotAvailableException exception) {
        return new ApiError(exception.getMessage(), "Not Available Exception.",
                HttpStatus.CONFLICT.getReasonPhrase().toUpperCase(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ApiError handleAlreadyExistsException(ExistsException exception) {
        return new ApiError(exception.getMessage(), "Already Exists Exception.",
                HttpStatus.CONFLICT.getReasonPhrase().toUpperCase(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiError handleRuntimeException(RuntimeException exception) {
        return new ApiError(exception.getMessage(), "Runtime Exception.",
                HttpStatus.BAD_REQUEST.getReasonPhrase().toUpperCase(), LocalDateTime.now());
    }

    @ExceptionHandler({ValidationException.class, MethodArgumentTypeMismatchException.class,
            MethodArgumentNotValidException.class, MissingServletRequestParameterException.class,
            DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiError handleBadRequestException(final RuntimeException e) {
        log.error("400 {} ", e.getMessage(), e);

        return new ApiError(e.getMessage(), "BAD_REQUEST",
                HttpStatus.BAD_REQUEST.getReasonPhrase().toUpperCase(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiError handleConstraintException(ConstraintViolationException exception) {
        return new ApiError(exception.getMessage(), "Constraint Violation",
                HttpStatus.BAD_REQUEST.getReasonPhrase().toUpperCase(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ApiError handleRaw(final Throwable e) {
        log.info("500: {} ", e.getMessage(), e);

        return new ApiError(e.getMessage(), "Internal Server Error.",
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase().toUpperCase(), LocalDateTime.now());
    }
}
