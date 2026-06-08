package com.kodelabs.formflow.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Lanzada cuando un recurso no existe o no pertenece al tenant activo.
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " con id '" + id + "' no encontrado", HttpStatus.NOT_FOUND);
    }
}
