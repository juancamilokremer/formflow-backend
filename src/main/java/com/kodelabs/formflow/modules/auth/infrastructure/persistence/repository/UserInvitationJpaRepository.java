package com.kodelabs.formflow.modules.auth.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.auth.domain.model.InvitationStatus;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.entity.UserInvitationJpaEntity;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserInvitationJpaRepository extends Repository<UserInvitationJpaEntity, UUID> {

    /** Flushed: @CreationTimestamp only populates the entity when the INSERT runs, and
     *  callers map the result to a DTO right away. See #122. */
    UserInvitationJpaEntity saveAndFlush(UserInvitationJpaEntity invitation);

    Optional<UserInvitationJpaEntity> findByTokenHash(String tokenHash);

    Optional<UserInvitationJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    List<UserInvitationJpaEntity> findAllByTenantIdAndStatus(UUID tenantId, InvitationStatus status);

    boolean existsByEmailAndTenantIdAndStatus(String email, UUID tenantId, InvitationStatus status);
}
