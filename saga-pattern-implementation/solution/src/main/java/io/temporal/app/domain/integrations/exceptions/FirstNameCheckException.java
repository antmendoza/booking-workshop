package io.temporal.app.domain.integrations.exceptions;

public class FirstNameCheckException extends RuntimeException {

    public FirstNameCheckException(String message) {
        super(message);
    }
}
