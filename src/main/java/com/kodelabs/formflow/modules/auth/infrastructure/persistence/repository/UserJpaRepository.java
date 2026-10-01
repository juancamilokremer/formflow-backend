package com.kodelabs.formflow.modules.auth.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends Repository<UserJpaEntity, UUID> {

    /** Flushed: @CreationTimestamp/@UpdateTimestamp only populate the entity when the
     *  INSERT runs, and callers map the result to a DTO right away. See #122. */
    UserJpaEntity saveAndFlush(UserJpaEntity user);

    Optional<UserJpaEntity> findByEmailAndTenantId(String email, UUID tenantId);

    Optional<UserJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByEmailAndTenantId(String email, UUID tenantId);

    Optional<UserJpaEntity> findFirstByTenantIdAndRole(UUID tenantId, UserRole role);

    /** Excludes SUPER_ADMIN — platform support accounts never count toward a tenant's
     *  plan usage. See #176. */
    long countByTenantIdAndActiveTrueAndRoleNot(UUID tenantId, UserRole role);

    /** Excludes SUPER_ADMIN — platform support accounts are invisible to the tenant's
     *  own team listing. See #176. */
    List<UserJpaEntity> findAllByTenantIdAndActiveTrueAndRoleNot(UUID tenantId, UserRole role);

    long countByTenantIdAndRoleAndActiveTrue(UUID tenantId, UserRole role);
}
