package com.kodelabs.formflow.modules.auth.domain.port;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para operaciones de persistencia de Tenant.
 */
public interface TenantRepositoryPort {

    Tenant save(Tenant tenant);

    Optional<Tenant> findById(UUID id);

    Optional<Tenant> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
