package com.kodelabs.formflow.modules.auth.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.auth.infrastructure.persistence.entity.TenantJpaEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface TenantJpaRepository extends Repository<TenantJpaEntity, UUID> {

    /** Flushed: @CreationTimestamp/@UpdateTimestamp only populate the entity when the
     *  INSERT runs, and callers map the result to a DTO right away. See #122. */
    TenantJpaEntity saveAndFlush(TenantJpaEntity tenant);

    Optional<TenantJpaEntity> findById(UUID id);

    Optional<TenantJpaEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
