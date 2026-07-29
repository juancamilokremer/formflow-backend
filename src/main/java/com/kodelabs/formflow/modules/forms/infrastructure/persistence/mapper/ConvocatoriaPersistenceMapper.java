package com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ScoringConfig;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.ConvocatoriaJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConvocatoriaPersistenceMapper {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public Convocatoria toDomain(ConvocatoriaJpaEntity entity) {
        ScoringConfig scoringConfig = objectMapper.readValue(
                entity.getScoringConfig(), ScoringConfig.class);
        return Convocatoria.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .type(FormType.valueOf(entity.getType()))
                .status(ConvocatoriaStatus.valueOf(entity.getStatus()))
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
                .name(domain.getName())
                .type(domain.getType().name())
                .status(domain.getStatus().name())
                .scoringConfig(objectMapper.writeValueAsString(domain.getScoringConfig()))
                .startDate(domain.getStartDate())
                .endDate(domain.getEndDate())
                .deletedAt(domain.getDeletedAt())
                .build();
    }
}
