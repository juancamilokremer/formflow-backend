package com.kodelabs.formflow.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a resource does not exist or does not belong to the active tenant.
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, Object id) {
        super("error.resource.not_found", HttpStatus.NOT_FOUND, resource, id);
    }
}
