package com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper;

import com.kodelabs.formflow.modules.forms.domain.model.Category;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.CategoryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryPersistenceMapper {

    public Category toDomain(CategoryJpaEntity e) {
        return Category.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .name(e.getName())
                .color(e.getColor())
                .description(e.getDescription())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public CategoryJpaEntity toEntity(Category c) {
        return CategoryJpaEntity.builder()
                .id(c.getId())
                .tenantId(c.getTenantId())
                .name(c.getName())
                .color(c.getColor())
                .description(c.getDescription())
                // See CandidatePersistenceMapper#toEntity: @CreationTimestamp only fires on
                // INSERT, so the domain value must be carried through for UPDATE paths.
                .createdAt(c.getCreatedAt())
                .build();
    }
}
