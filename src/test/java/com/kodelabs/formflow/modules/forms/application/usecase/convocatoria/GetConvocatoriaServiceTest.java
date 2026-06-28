package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetConvocatoriaQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetConvocatoriaServiceTest {

    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private CandidateRepositoryPort candidateRepository;
    @InjectMocks private GetConvocatoriaService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID convId   = UUID.randomUUID();

    @Test
    void returnsConvocatoriaWithCandidates() {
        Convocatoria conv = Convocatoria.builder().id(convId).tenantId(tenantId)
                .formId(UUID.randomUUID()).name("Analista RRHH").status(ConvocatoriaStatus.ACTIVE).build();
        Candidate candidate = Candidate.builder().id(UUID.randomUUID()).convocatoriaId(convId)
                .tenantId(tenantId).name("María G.").email("maria@test.com")
                .status(CandidateStatus.INVITED).token(UUID.randomUUID()).build();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(conv));
        when(candidateRepository.findAllByConvocatoriaId(convId)).thenReturn(List.of(candidate));

        ConvocatoriaResult result = service.execute(new GetConvocatoriaQuery(convId, tenantId));

        assertThat(result.id()).isEqualTo(convId);
        assertThat(result.name()).isEqualTo("Analista RRHH");
        assertThat(result.candidates()).hasSize(1);
        assertThat(result.candidates().get(0).name()).isEqualTo("María G.");
    }

    @Test
    void throwsNotFoundWhenConvocatoriaDoesNotBelongToTenant() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.empty());

        var query = new GetConvocatoriaQuery(convId, tenantId);
        assertThatThrownBy(() -> service.execute(query))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
