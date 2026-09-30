package com.kodelabs.formflow.modules.auth.infrastructure.persistence.mapper;

import com.kodelabs.formflow.modules.auth.domain.model.UserInvitation;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.entity.UserInvitationJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Converts between the UserInvitation domain model and the UserInvitationJpaEntity JPA entity.
 */
@Component
public class UserInvitationPersistenceMapper {

    public UserInvitation toDomain(UserInvitationJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserInvitation.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .email(entity.getEmail())
                .role(entity.getRole())
                .tokenHash(entity.getTokenHash())
                .status(entity.getStatus())
                .invitedByUserId(entity.getInvitedByUserId())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public UserInvitationJpaEntity toEntity(UserInvitation domain) {
        if (domain == null) {
            return null;
        }
        return UserInvitationJpaEntity.builder()
                .id(domain.getId())
                .tenantId(domain.getTenantId())
                .email(domain.getEmail())
                .role(domain.getRole())
                .tokenHash(domain.getTokenHash())
                .status(domain.getStatus())
                .invitedByUserId(domain.getInvitedByUserId())
                .expiresAt(domain.getExpiresAt())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
