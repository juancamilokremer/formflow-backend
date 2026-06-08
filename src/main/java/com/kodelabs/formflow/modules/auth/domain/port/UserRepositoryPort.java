package com.kodelabs.formflow.modules.auth.domain.port;

import com.kodelabs.formflow.modules.auth.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida: define qué operaciones de persistencia necesita el dominio.
 * El dominio solo conoce esta interfaz, nunca la implementación JPA.
 */
public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findByEmailAndTenantId(String email, UUID tenantId);

    Optional<User> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByEmailAndTenantId(String email, UUID tenantId);
}
