package com.resumeiq.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Custom base exception class for API-driven validation or business logic errors.
 */
@Getter
public class CustomException extends RuntimeException {
    private final HttpStatus status;

    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
