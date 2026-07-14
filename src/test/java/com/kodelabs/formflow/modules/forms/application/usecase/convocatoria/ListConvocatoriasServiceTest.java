package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ListConvocatoriasQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaSummaryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListConvocatoriasServiceTest {

    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private CandidateRepositoryPort candidateRepository;
    @InjectMocks private ListConvocatoriasService service;

    private final UUID tenantId = UUID.randomUUID();

    @Test
    void returnsConvocatoriasWithCandidateCount() {
        UUID conv1Id = UUID.randomUUID();
        UUID conv2Id = UUID.randomUUID();
        Convocatoria c1 = Convocatoria.builder().id(conv1Id).tenantId(tenantId)
                .formId(UUID.randomUUID()).name("Proceso A").status(ConvocatoriaStatus.ACTIVE).build();
        Convocatoria c2 = Convocatoria.builder().id(conv2Id).tenantId(tenantId)
                .formId(UUID.randomUUID()).name("Proceso B").status(ConvocatoriaStatus.DRAFT).build();
        when(convocatoriaRepository.findActiveByTenantId(tenantId)).thenReturn(List.of(c1, c2));
        when(candidateRepository.countByConvocatoriaId(conv1Id)).thenReturn(5L);
        when(candidateRepository.countByConvocatoriaId(conv2Id)).thenReturn(0L);
        when(candidateRepository.countRespondedByConvocatoriaId(conv1Id)).thenReturn(3L);
        when(candidateRepository.countRespondedByConvocatoriaId(conv2Id)).thenReturn(0L);

        List<ConvocatoriaSummaryResult> results = service.execute(new ListConvocatoriasQuery(tenantId));

        assertThat(results).hasSize(2);
        assertThat(results.get(0).name()).isEqualTo("Proceso A");
        assertThat(results.get(0).candidateCount()).isEqualTo(5L);
        assertThat(results.get(0).respondedCount()).isEqualTo(3L);
        assertThat(results.get(1).candidateCount()).isZero();
        assertThat(results.get(1).respondedCount()).isZero();
    }

    @Test
    void returnsEmptyListWhenNoConvocatoriasExist() {
        when(convocatoriaRepository.findActiveByTenantId(tenantId)).thenReturn(List.of());

        List<ConvocatoriaSummaryResult> results = service.execute(new ListConvocatoriasQuery(tenantId));

        assertThat(results).isEmpty();
    }
}
