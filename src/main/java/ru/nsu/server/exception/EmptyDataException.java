package ru.nsu.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

//Когда пытаются сделать изменения
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EmptyDataException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public EmptyDataException(String message) {
        super(String.format("Ошибка! %s", message));
    }
}
