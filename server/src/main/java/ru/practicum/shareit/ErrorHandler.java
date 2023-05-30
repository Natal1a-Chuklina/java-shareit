package ru.practicum.shareit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.utils.ErrorResponse;

import static ru.practicum.shareit.utils.Constants.UNKNOWN_ERROR_MESSAGE;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler({NotAvailableException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleRuntimeBadRequestExceptions(RuntimeException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({NotFoundException.class, SecurityException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundRuntimeExceptions(RuntimeException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleAlreadyExistException(AlreadyExistException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(Throwable e) {
        log.error("Произошла неизвестная ошибка: {}", e.getMessage(), e);
        return new ErrorResponse(UNKNOWN_ERROR_MESSAGE);
    }
}
