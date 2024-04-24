package ru.nsu.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

//Когда пытаются сделать изменения
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictChangesException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ConflictChangesException(String message) {
        super(String.format("Ошибка! %s", message));
    }
}
