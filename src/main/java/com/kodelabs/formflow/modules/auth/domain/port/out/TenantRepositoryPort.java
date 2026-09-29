package com.kodelabs.formflow.modules.auth.domain.port.out;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.model.TenantStatus;

import java.util.List;
import java.util.Map;
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

    /** status/plan may each be null to skip that filter. */
    List<Tenant> findAll(int page, int size, TenantStatus status, TenantPlan plan);

    long countAll(TenantStatus status, TenantPlan plan);

    Map<TenantPlan, Long> countByPlan();
}
