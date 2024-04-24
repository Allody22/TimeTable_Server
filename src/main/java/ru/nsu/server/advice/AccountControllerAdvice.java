package ru.nsu.server.advice;


import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.nsu.server.exception.ConflictChangesException;
import ru.nsu.server.exception.EmptyDataException;
import ru.nsu.server.exception.NotInDataBaseException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AccountControllerAdvice {

    @ExceptionHandler(value = EmptyDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleUnavailableChangesException(EmptyDataException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return errorResponse;
    }

    @ExceptionHandler(value = ConflictChangesException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleUnavailableChangesException(ConflictChangesException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return errorResponse;
    }

    @ExceptionHandler(value = NotInDataBaseException.class)
    @ResponseStatus(HttpStatus.GONE)
    public Map<String, String> handleNotInDataBaseException(NotInDataBaseException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return errorResponse;
    }
}
