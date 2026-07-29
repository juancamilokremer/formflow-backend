package com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.ConvocatoriaFormJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConvocatoriaFormPersistenceMapper {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public ConvocatoriaForm toDomain(ConvocatoriaFormJpaEntity entity) {
        List<CategoryWeight> weights = objectMapper.readValue(
                entity.getCategoryWeights(), new TypeReference<>() {});
        return ConvocatoriaForm.builder()
                .id(entity.getId())
                .convocatoriaId(entity.getConvocatoriaId())
                .formId(entity.getFormId())
                .weight(entity.getWeight())
                .categoryWeights(weights)
                .minScore(entity.getMinScore())
                .position(entity.getPosition())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @SneakyThrows
    public ConvocatoriaFormJpaEntity toEntity(ConvocatoriaForm domain) {
        return ConvocatoriaFormJpaEntity.builder()
                .id(domain.getId())
                .convocatoriaId(domain.getConvocatoriaId())
                .formId(domain.getFormId())
                .weight(domain.getWeight())
                .categoryWeights(objectMapper.writeValueAsString(domain.getCategoryWeights()))
                .minScore(domain.getMinScore())
                .position(domain.getPosition())
                .build();
    }
}
