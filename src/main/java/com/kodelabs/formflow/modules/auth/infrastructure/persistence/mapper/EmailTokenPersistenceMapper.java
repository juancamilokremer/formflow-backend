package com.kodelabs.formflow.modules.auth.infrastructure.persistence.mapper;

import com.kodelabs.formflow.modules.auth.domain.model.EmailToken;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.entity.EmailTokenJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Converts between the EmailToken domain model and the EmailTokenJpaEntity JPA entity.
 */
@Component
public class EmailTokenPersistenceMapper {

    public EmailToken toDomain(EmailTokenJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return EmailToken.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .tenantId(entity.getTenantId())
                .tokenHash(entity.getTokenHash())
                .type(entity.getType())
                .expiresAt(entity.getExpiresAt())
                .usedAt(entity.getUsedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public EmailTokenJpaEntity toEntity(EmailToken domain) {
        if (domain == null) {
            return null;
        }
        return EmailTokenJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .tenantId(domain.getTenantId())
                .tokenHash(domain.getTokenHash())
                .type(domain.getType())
                .expiresAt(domain.getExpiresAt())
                .usedAt(domain.getUsedAt())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
