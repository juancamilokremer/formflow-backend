package com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper;

import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.FormSectionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class FormSectionPersistenceMapper {

    public FormSection toDomain(FormSectionJpaEntity e) {
        return FormSection.builder()
                .id(e.getId())
                .formId(e.getFormId())
                .tenantId(e.getTenantId())
                .title(e.getTitle())
                .description(e.getDescription())
                .position(e.getPosition())
                .timeLimitSeconds(e.getTimeLimitSeconds())
                .deletedAt(e.getDeletedAt())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public FormSectionJpaEntity toEntity(FormSection s) {
        return FormSectionJpaEntity.builder()
                .id(s.getId())
                .formId(s.getFormId())
                .tenantId(s.getTenantId())
                .title(s.getTitle())
                .description(s.getDescription())
                .position(s.getPosition())
                .timeLimitSeconds(s.getTimeLimitSeconds())
                .deletedAt(s.getDeletedAt())
                .build();
    }
}
