package com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ScoringConfig;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.ConvocatoriaJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConvocatoriaPersistenceMapper {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public Convocatoria toDomain(ConvocatoriaJpaEntity entity) {
        List<CategoryWeight> weights = objectMapper.readValue(
                entity.getCategoryWeights(), new TypeReference<>() {});
        ScoringConfig scoringConfig = objectMapper.readValue(
                entity.getScoringConfig(), ScoringConfig.class);
        return Convocatoria.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .formId(entity.getFormId())
                .name(entity.getName())
                .type(FormType.valueOf(entity.getType()))
                .status(ConvocatoriaStatus.valueOf(entity.getStatus()))
                .categoryWeights(weights)
                .scoringConfig(scoringConfig)
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .deletedAt(entity.getDeletedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @SneakyThrows
    public ConvocatoriaJpaEntity toEntity(Convocatoria domain) {
        return ConvocatoriaJpaEntity.builder()
                .id(domain.getId())
                .tenantId(domain.getTenantId())
                .formId(domain.getFormId())
                .name(domain.getName())
                .type(domain.getType().name())
                .status(domain.getStatus().name())
                .categoryWeights(objectMapper.writeValueAsString(domain.getCategoryWeights()))
                .scoringConfig(objectMapper.writeValueAsString(domain.getScoringConfig()))
                .startDate(domain.getStartDate())
                .endDate(domain.getEndDate())
                .deletedAt(domain.getDeletedAt())
                .build();
    }
}
