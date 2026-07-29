package com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ScoringConfig;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.ConvocatoriaJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConvocatoriaPersistenceMapperTest {

    private ConvocatoriaPersistenceMapper mapper;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mapper = new ConvocatoriaPersistenceMapper(objectMapper);
    }

    @Test
    void roundTripPreservesAllFields() {
        UUID id       = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        Convocatoria domain = Convocatoria.builder()
                .id(id).tenantId(tenantId).name("Test")
                .type(FormType.CANDIDATES)
                .status(ConvocatoriaStatus.ACTIVE)
                .scoringConfig(new ScoringConfig(75, 55))
                .startDate(Instant.now()).build();

        ConvocatoriaJpaEntity entity = mapper.toEntity(domain);
        Convocatoria restored        = mapper.toDomain(entity);

        assertThat(restored.getId()).isEqualTo(id);
        assertThat(restored.getName()).isEqualTo("Test");
        assertThat(restored.getStatus()).isEqualTo(ConvocatoriaStatus.ACTIVE);
        assertThat(restored.getScoringConfig().aptoMin()).isEqualTo(75);
        assertThat(restored.getScoringConfig().revisarMin()).isEqualTo(55);
        assertThat(restored.getType()).isEqualTo(FormType.CANDIDATES);
    }
}
