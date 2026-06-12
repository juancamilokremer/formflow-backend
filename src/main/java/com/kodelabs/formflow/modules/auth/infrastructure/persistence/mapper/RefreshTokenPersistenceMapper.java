package com.kodelabs.formflow.modules.auth.infrastructure.persistence.mapper;

import com.kodelabs.formflow.modules.auth.domain.model.RefreshToken;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Converts between the RefreshToken domain model and the RefreshTokenJpaEntity JPA entity.
 */
@Component
public class RefreshTokenPersistenceMapper {

    public RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return RefreshToken.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .tenantId(entity.getTenantId())
                .tokenHash(entity.getTokenHash())
                .expiresAt(entity.getExpiresAt())
                .revokedAt(entity.getRevokedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public RefreshTokenJpaEntity toEntity(RefreshToken domain) {
        if (domain == null) {
            return null;
        }
        return RefreshTokenJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .tenantId(domain.getTenantId())
                .tokenHash(domain.getTokenHash())
                .expiresAt(domain.getExpiresAt())
                .revokedAt(domain.getRevokedAt())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
