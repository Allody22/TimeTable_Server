package ru.nsu.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.GONE)
public class NotInDataBaseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public NotInDataBaseException(String message) {
        super(String.format("Ошибка! %s", message));
    }
}
