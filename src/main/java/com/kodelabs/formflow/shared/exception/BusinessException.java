package com.kodelabs.formflow.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for business errors.
 * Associates an HTTP status code with each domain error.
 * Messages are user-facing and therefore stay in Spanish (MessageSource in #26).
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public BusinessException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
