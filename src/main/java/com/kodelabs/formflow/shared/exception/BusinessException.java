package com.kodelabs.formflow.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for business errors.
 * Carries a message KEY (resolved against MessageSource by the
 * GlobalExceptionHandler) plus optional MessageFormat arguments —
 * never a final user-facing text.
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final transient Object[] args;

    public BusinessException(String messageKey, HttpStatus status, Object... args) {
        super(messageKey);
        this.status = status;
        this.args = args;
    }

    public BusinessException(String messageKey) {
        this(messageKey, HttpStatus.BAD_REQUEST);
    }

    public String getMessageKey() {
        return getMessage();
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Object[] getArgs() {
        return args;
    }
}
