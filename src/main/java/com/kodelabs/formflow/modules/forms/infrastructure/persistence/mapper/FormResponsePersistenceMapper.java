package com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.FormSnapshot;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.FormResponseJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class FormResponsePersistenceMapper {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public FormResponse toDomain(FormResponseJpaEntity entity) {
        FormSnapshot snapshot = objectMapper.readValue(entity.getFormSnapshot(), FormSnapshot.class);
        return FormResponse.builder()
                .id(entity.getId())
                .formId(entity.getFormId())
                .tenantId(entity.getTenantId())
                .convocatoriaId(entity.getConvocatoriaId())
                .respondentToken(entity.getRespondentToken())
                .formSnapshot(snapshot)
                .submittedAt(entity.getSubmittedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @SneakyThrows
    public FormResponseJpaEntity toEntity(FormResponse domain) {
        return FormResponseJpaEntity.builder()
                .id(domain.getId())
                .formId(domain.getFormId())
                .tenantId(domain.getTenantId())
                .convocatoriaId(domain.getConvocatoriaId())
                .respondentToken(domain.getRespondentToken())
                .formSnapshot(objectMapper.writeValueAsString(domain.getFormSnapshot()))
                .submittedAt(domain.getSubmittedAt() != null ? domain.getSubmittedAt() : Instant.now())
                .build();
    }
}
