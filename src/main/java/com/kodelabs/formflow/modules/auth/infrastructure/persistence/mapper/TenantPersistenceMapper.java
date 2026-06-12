package com.kodelabs.formflow.modules.auth.infrastructure.persistence.mapper;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.entity.TenantJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Convierte entre el modelo de dominio Tenant y la entidad JPA TenantJpaEntity.
 */
@Component
public class TenantPersistenceMapper {

    public Tenant toDomain(TenantJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Tenant.builder()
                .id(entity.getId())
                .slug(entity.getSlug())
                .name(entity.getName())
                .logoUrl(entity.getLogoUrl())
                .primaryColor(entity.getPrimaryColor())
                .secondaryColor(entity.getSecondaryColor())
                .plan(entity.getPlan())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public TenantJpaEntity toEntity(Tenant domain) {
        if (domain == null) {
            return null;
        }
        return TenantJpaEntity.builder()
                .id(domain.getId())
                .slug(domain.getSlug())
                .name(domain.getName())
                .logoUrl(domain.getLogoUrl())
                .primaryColor(domain.getPrimaryColor())
                .secondaryColor(domain.getSecondaryColor())
                .plan(domain.getPlan())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
