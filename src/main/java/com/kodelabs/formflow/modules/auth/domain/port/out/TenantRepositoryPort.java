package com.kodelabs.formflow.modules.auth.domain.port.out;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;

import java.util.Optional;
import java.util.UUID;

/**
 * Output port for Tenant persistence operations.
 */
public interface TenantRepositoryPort {

    Tenant save(Tenant tenant);

    Optional<Tenant> findById(UUID id);

    Optional<Tenant> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
