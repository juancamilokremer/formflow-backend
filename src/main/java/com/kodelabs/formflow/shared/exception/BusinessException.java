package com.kodelabs.formflow.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción base para errores de negocio.
 * Permite asociar un HTTP status code a cada error de dominio.
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
