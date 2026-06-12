package com.kodelabs.formflow.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a resource does not exist or does not belong to the active tenant.
 * The exception message is user-facing and therefore stays in Spanish.
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " con id '" + id + "' no encontrado", HttpStatus.NOT_FOUND);
    }
}
