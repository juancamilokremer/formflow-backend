package com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.FormJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class FormPersistenceMapper {

    public Form toDomain(FormJpaEntity e) {
        return Form.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .name(e.getName())
                .description(e.getDescription())
                .type(FormType.valueOf(e.getType()))
                .version(e.getVersion())
                .timeLimitSeconds(e.getTimeLimitSeconds())
                .deletedAt(e.getDeletedAt())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public FormJpaEntity toEntity(Form f) {
        return FormJpaEntity.builder()
                .id(f.getId())
                .tenantId(f.getTenantId())
                .name(f.getName())
                .description(f.getDescription())
                .type(f.getType().name())
                .version(f.getVersion())
                .timeLimitSeconds(f.getTimeLimitSeconds())
                .deletedAt(f.getDeletedAt())
                .createdBy(f.getCreatedBy())
                .updatedBy(f.getUpdatedBy())
                .build();
    }
}
