package com.kodelabs.formflow.modules.forms.application.usecase.form;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetPublicFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetPublicCandidateFormQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.PublicCandidateFormResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.PublicFormResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPublicCandidateFormServiceTest {

    @Mock private CandidateRepositoryPort candidateRepository;
    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private GetPublicFormUseCase getPublicForm;
    @InjectMocks private GetPublicCandidateFormService service;

    private UUID candidateToken;
    private UUID convocatoriaId;
    private UUID tenantId;
    private UUID formId;
    private Candidate invitedCandidate;
    private Convocatoria activeConvocatoria;
    private PublicFormResult mockFormResult;

    @BeforeEach
    void setUp() {
        candidateToken  = UUID.randomUUID();
        convocatoriaId  = UUID.randomUUID();
        tenantId        = UUID.randomUUID();
        formId          = UUID.randomUUID();

        invitedCandidate = Candidate.builder()
                .id(UUID.randomUUID())
                .token(candidateToken)
                .convocatoriaId(convocatoriaId)
                .tenantId(tenantId)
                .name("María García")
                .email("maria@test.com")
                .status(CandidateStatus.INVITED)
                .build();

        activeConvocatoria = Convocatoria.builder()
                .id(convocatoriaId)
                .tenantId(tenantId)
                .formId(formId)
                .name("Analista RRHH — Julio 2026")
                .status(ConvocatoriaStatus.ACTIVE)
                .build();

        mockFormResult = new PublicFormResult(
                formId, "Evaluación de aspirantes", null, null,
                "Acme Corp", null, null, List.of());
    }

    @Test
    void happyPath_returnsFormWithCandidateInfo() {
        when(candidateRepository.findByToken(candidateToken)).thenReturn(Optional.of(invitedCandidate));
        when(convocatoriaRepository.findByIdAndTenantId(convocatoriaId, tenantId))
                .thenReturn(Optional.of(activeConvocatoria));
        when(getPublicForm.execute(any())).thenReturn(mockFormResult);

        PublicCandidateFormResult result = service.execute(new GetPublicCandidateFormQuery(candidateToken));

        assertThat(result.candidateName()).isEqualTo("María García");
        assertThat(result.convocatoriaName()).isEqualTo("Analista RRHH — Julio 2026");
        assertThat(result.alreadyResponded()).isFalse();
        assertThat(result.form().formId()).isEqualTo(formId);
    }

    @Test
    void alreadyRespondedCandidate_returnsAlreadyRespondedTrue() {
        Candidate responded = Candidate.builder()
                .id(UUID.randomUUID()).token(candidateToken)
                .convocatoriaId(convocatoriaId).tenantId(tenantId)
                .name("María García").status(CandidateStatus.RESPONDED).build();

        when(candidateRepository.findByToken(candidateToken)).thenReturn(Optional.of(responded));
        when(convocatoriaRepository.findByIdAndTenantId(convocatoriaId, tenantId))
                .thenReturn(Optional.of(activeConvocatoria));
        when(getPublicForm.execute(any())).thenReturn(mockFormResult);

        PublicCandidateFormResult result = service.execute(new GetPublicCandidateFormQuery(candidateToken));

        assertThat(result.alreadyResponded()).isTrue();
    }

    @Test
    void tokenNotFound_throwsNotFound() {
        when(candidateRepository.findByToken(candidateToken)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new GetPublicCandidateFormQuery(candidateToken)))
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
                .id(convocatoriaId).tenantId(tenantId).formId(formId)
                .name("Test").status(ConvocatoriaStatus.CLOSED).build();

        when(candidateRepository.findByToken(candidateToken)).thenReturn(Optional.of(invitedCandidate));
        when(convocatoriaRepository.findByIdAndTenantId(convocatoriaId, tenantId))
                .thenReturn(Optional.of(closed));

        assertThatThrownBy(() -> service.execute(new GetPublicCandidateFormQuery(candidateToken)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(be.getMessageKey()).isEqualTo("error.convocatoria.closed");
                });
    }
}
