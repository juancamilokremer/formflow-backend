package com.kodelabs.formflow.modules.forms.application.usecase.form;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetCandidatePortalQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidatePortalResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
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
class GetCandidatePortalServiceTest {

    @Mock private CandidateRepositoryPort candidateRepository;
    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private FormRepositoryPort formRepository;
    @Mock private FormResponseRepositoryPort responseRepository;
    @InjectMocks private GetCandidatePortalService service;

    private UUID candidateToken;
    private UUID convocatoriaId;
    private UUID tenantId;
    private UUID formId1;
    private UUID formId2;
    private Candidate candidate;

    @BeforeEach
    void setUp() {
        candidateToken = UUID.randomUUID();
        convocatoriaId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        formId1 = UUID.randomUUID();
        formId2 = UUID.randomUUID();

        candidate = Candidate.builder()
                .id(UUID.randomUUID())
                .token(candidateToken)
                .convocatoriaId(convocatoriaId)
                .tenantId(tenantId)
                .name("María García")
                .status(CandidateStatus.IN_PROGRESS)
                .build();
    }

    @Test
    void returnsFormsWithCompletionStatusOrderedByPosition() {
        Convocatoria convocatoria = Convocatoria.builder()
                .id(convocatoriaId).tenantId(tenantId).name("Analista RRHH")
                .status(ConvocatoriaStatus.ACTIVE)
                .forms(List.of(
                        ConvocatoriaForm.builder().formId(formId1).weight(60).position(0).build(),
                        ConvocatoriaForm.builder().formId(formId2).weight(40).position(1).build()))
                .build();

        when(candidateRepository.findByToken(candidateToken)).thenReturn(Optional.of(candidate));
        when(convocatoriaRepository.findByIdAndTenantId(convocatoriaId, tenantId))
                .thenReturn(Optional.of(convocatoria));
        when(formRepository.findByIdAndTenantId(formId1, tenantId))
                .thenReturn(Optional.of(Form.builder().id(formId1).tenantId(tenantId).name("Prueba técnica").build()));
        when(formRepository.findByIdAndTenantId(formId2, tenantId))
                .thenReturn(Optional.of(Form.builder().id(formId2).tenantId(tenantId).name("Perfil").build()));
        when(responseRepository.existsByCandidateIdAndFormId(candidate.getId(), formId1)).thenReturn(true);
        when(responseRepository.existsByCandidateIdAndFormId(candidate.getId(), formId2)).thenReturn(false);

        CandidatePortalResult result = service.execute(new GetCandidatePortalQuery(candidateToken));

        assertThat(result.candidateName()).isEqualTo("María García");
        assertThat(result.convocatoriaName()).isEqualTo("Analista RRHH");
        assertThat(result.allCompleted()).isFalse();
        assertThat(result.forms()).hasSize(2);
        assertThat(result.forms().get(0).name()).isEqualTo("Prueba técnica");
        assertThat(result.forms().get(0).completed()).isTrue();
        assertThat(result.forms().get(1).name()).isEqualTo("Perfil");
        assertThat(result.forms().get(1).completed()).isFalse();
    }

    @Test
    void allCompletedWhenCandidateStatusIsResponded() {
        Convocatoria convocatoria = Convocatoria.builder()
                .id(convocatoriaId).tenantId(tenantId).name("Analista RRHH")
                .status(ConvocatoriaStatus.ACTIVE)
                .forms(List.of(ConvocatoriaForm.builder().formId(formId1).weight(100).position(0).build()))
                .build();
        Candidate responded = Candidate.builder()
                .id(UUID.randomUUID()).token(candidateToken).convocatoriaId(convocatoriaId).tenantId(tenantId)
                .name("María García").status(CandidateStatus.RESPONDED).build();

        when(candidateRepository.findByToken(candidateToken)).thenReturn(Optional.of(responded));
        when(convocatoriaRepository.findByIdAndTenantId(convocatoriaId, tenantId))
                .thenReturn(Optional.of(convocatoria));
        when(formRepository.findByIdAndTenantId(formId1, tenantId))
                .thenReturn(Optional.of(Form.builder().id(formId1).tenantId(tenantId).name("Prueba técnica").build()));
        when(responseRepository.existsByCandidateIdAndFormId(responded.getId(), formId1)).thenReturn(true);

        CandidatePortalResult result = service.execute(new GetCandidatePortalQuery(candidateToken));

        assertThat(result.allCompleted()).isTrue();
    }

    @Test
    void tokenNotFound_throwsNotFound() {
        when(candidateRepository.findByToken(candidateToken)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new GetCandidatePortalQuery(candidateToken)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(be.getMessageKey()).isEqualTo("error.candidate.token_not_found");
                });
    }

    @Test
    void closedConvocatoria_throwsConflict() {
        Convocatoria closed = Convocatoria.builder()
                .id(convocatoriaId).tenantId(tenantId).name("Test").status(ConvocatoriaStatus.CLOSED)
                .forms(List.of(ConvocatoriaForm.builder().formId(formId1).weight(100).build()))
                .build();

        when(candidateRepository.findByToken(candidateToken)).thenReturn(Optional.of(candidate));
        when(convocatoriaRepository.findByIdAndTenantId(convocatoriaId, tenantId))
                .thenReturn(Optional.of(closed));

        assertThatThrownBy(() -> service.execute(new GetCandidatePortalQuery(candidateToken)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(be.getMessageKey()).isEqualTo("error.convocatoria.closed");
                });
    }
}
