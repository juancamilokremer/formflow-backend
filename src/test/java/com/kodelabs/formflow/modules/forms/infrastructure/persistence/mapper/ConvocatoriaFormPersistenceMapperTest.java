package com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.ConvocatoriaFormJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConvocatoriaFormPersistenceMapperTest {

    private ConvocatoriaFormPersistenceMapper mapper;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mapper = new ConvocatoriaFormPersistenceMapper(objectMapper);
    }

    @Test
    void roundTripPreservesAllFields() {
        UUID id             = UUID.randomUUID();
        UUID convocatoriaId = UUID.randomUUID();
        UUID formId         = UUID.randomUUID();
        UUID catId          = UUID.randomUUID();

        ConvocatoriaForm domain = ConvocatoriaForm.builder()
                .id(id).convocatoriaId(convocatoriaId).formId(formId)
                .weight(60)
                .categoryWeights(List.of(new CategoryWeight(catId, 100)))
                .minScore(50)
                .position(1)
                .build();

        ConvocatoriaFormJpaEntity entity = mapper.toEntity(domain);
        ConvocatoriaForm restored        = mapper.toDomain(entity);

        assertThat(restored.getId()).isEqualTo(id);
        assertThat(restored.getConvocatoriaId()).isEqualTo(convocatoriaId);
        assertThat(restored.getFormId()).isEqualTo(formId);
        assertThat(restored.getWeight()).isEqualTo(60);
        assertThat(restored.getCategoryWeights()).hasSize(1);
        assertThat(restored.getCategoryWeights().get(0).categoryId()).isEqualTo(catId);
        assertThat(restored.getMinScore()).isEqualTo(50);
        assertThat(restored.getPosition()).isEqualTo(1);
    }

    @Test
    void roundTripWithEmptyWeightsPreservesEmptyList() {
        ConvocatoriaForm domain = ConvocatoriaForm.builder()
                .id(UUID.randomUUID()).convocatoriaId(UUID.randomUUID()).formId(UUID.randomUUID())
                .weight(100).categoryWeights(List.of()).position(0)
                .build();

        ConvocatoriaForm restored = mapper.toDomain(mapper.toEntity(domain));

        assertThat(restored.getCategoryWeights()).isEmpty();
    }
}
