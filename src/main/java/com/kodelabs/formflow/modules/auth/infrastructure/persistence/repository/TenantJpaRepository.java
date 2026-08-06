package com.kodelabs.formflow.modules.auth.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.auth.infrastructure.persistence.entity.TenantJpaEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface TenantJpaRepository extends Repository<TenantJpaEntity, UUID> {

    TenantJpaEntity save(TenantJpaEntity tenant);

    Optional<TenantJpaEntity> findById(UUID id);

    Optional<TenantJpaEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
