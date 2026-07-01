package com.kodelabs.formflow.modules.auth.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByEmailAndTenantId(String email, UUID tenantId);

    Optional<UserJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByEmailAndTenantId(String email, UUID tenantId);

    Optional<UserJpaEntity> findFirstByTenantIdAndRole(UUID tenantId, UserRole role);
}
