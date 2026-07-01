package com.kodelabs.formflow.modules.forms.application.usecase.response;

import com.kodelabs.formflow.modules.forms.application.service.CandidateScoringService;
import com.kodelabs.formflow.modules.forms.application.service.ConditionalLogicEvaluator;
import com.kodelabs.formflow.modules.forms.application.service.FormSnapshotBuilder;
import com.kodelabs.formflow.modules.forms.application.service.ScoringResult;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.FormSnapshot;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.AnswerItem;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.SubmitCandidateResponseCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SubmitCandidateResponseResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitCandidateResponseServiceTest {

    @Mock private CandidateRepositoryPort candidateRepository;
    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private FormLoader formLoader;
    @Mock private FormResponseRepositoryPort responseRepository;
    @Mock private FormSnapshotBuilder snapshotBuilder;
    @Mock private ConditionalLogicEvaluator conditionalLogicEvaluator;
    @Mock private CandidateScoringService candidateScoringService;
    @InjectMocks private SubmitCandidateResponseService service;

    private UUID candidateToken;
    private UUID convocatoriaId;
    private UUID formId;
    private UUID tenantId;
    private UUID questionId;
    private Candidate invitedCandidate;
    private Convocatoria activeConvocatoria;
    private Form activeForm;
    private FormResponse savedResponse;
    private FormSnapshot snapshot;

    @BeforeEach
    void setUp() {
        candidateToken = UUID.randomUUID();
        convocatoriaId = UUID.randomUUID();
        formId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        questionId = UUID.randomUUID();

        invitedCandidate = Candidate.builder()
                .id(UUID.randomUUID())
                .token(candidateToken)
                .convocatoriaId(convocatoriaId)
                .tenantId(tenantId)
                .status(CandidateStatus.INVITED)
                .build();

        activeConvocatoria = Convocatoria.builder()
                .id(convocatoriaId)
                .tenantId(tenantId)
                .formId(formId)
                .status(ConvocatoriaStatus.ACTIVE)
                .categoryWeights(List.of(new CategoryWeight(UUID.randomUUID(), 100)))
                .build();

        FormQuestion question = FormQuestion.builder()
                .id(questionId)
                .title("¿Años de experiencia?")
                .type(QuestionType.of("single"))
                .required(true)
                .position(1)
                .build();

        FormSection section = FormSection.builder()
                .id(UUID.randomUUID())
                .position(1)
                .questions(List.of(question))
                .build();

        activeForm = Form.builder()
                .id(formId)
                .tenantId(tenantId)
                .status(FormStatus.ACTIVE)
                .version(1)
                .sections(List.of(section))
                .build();

        savedResponse = FormResponse.builder()
                .id(UUID.randomUUID())
                .respondentToken(UUID.randomUUID())
                .build();

        snapshot = new FormSnapshot(formId, "Test Form", "CANDIDATES", 1, Instant.now(), List.of());
    }

    @Test
    void happyPath_savesResponseUpdatesCandidate_returnsToken() {
        when(candidateRepository.findByToken(candidateToken)).thenReturn(Optional.of(invitedCandidate));
        when(convocatoriaRepository.findByIdAndTenantId(convocatoriaId, tenantId))
                .thenReturn(Optional.of(activeConvocatoria));
        when(formLoader.loadPublicOrThrow(formId)).thenReturn(activeForm);
        when(conditionalLogicEvaluator.isVisible(any(), any(Map.class))).thenReturn(true);
        when(candidateScoringService.compute(any(), any(), any()))
                .thenReturn(new ScoringResult(85.0, Map.of()));
        when(snapshotBuilder.buildFromForm(activeForm)).thenReturn(snapshot);
        when(responseRepository.save(any())).thenReturn(savedResponse);
        when(candidateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SubmitCandidateResponseCommand command = new SubmitCandidateResponseCommand(
                candidateToken, null, List.of(new AnswerItem(questionId, "opt-1")));

        SubmitCandidateResponseResult result = service.execute(command);

        assertThat(result.respondentToken()).isEqualTo(savedResponse.getRespondentToken());

        ArgumentCaptor<Candidate> captor = ArgumentCaptor.forClass(Candidate.class);
        verify(candidateRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(CandidateStatus.RESPONDED);
        assertThat(captor.getValue().getScores()).isNotNull();
        assertThat(captor.getValue().getScores().total()).isEqualTo(85.0);
        assertThat(captor.getValue().getRespondedAt()).isNotNull();
    }

    @Test
    void candidateTokenNotFound_throwsNotFound() {
        when(candidateRepository.findByToken(candidateToken)).thenReturn(Optional.empty());

        var command = new SubmitCandidateResponseCommand(candidateToken, null, List.of());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void candidateAlreadyResponded_throwsConflict() {
        Candidate responded = Candidate.builder()
                .token(candidateToken)
                .convocatoriaId(convocatoriaId)
                .tenantId(tenantId)
                .status(CandidateStatus.RESPONDED)
                .build();
        when(candidateRepository.findByToken(candidateToken)).thenReturn(Optional.of(responded));

        var command = new SubmitCandidateResponseCommand(candidateToken, null, List.of());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(be.getMessageKey()).isEqualTo("error.candidate.already_responded");
                });
    }

    @Test
    void convocatoriaNotActive_throwsConflict() {
        Convocatoria closed = Convocatoria.builder()
                .id(convocatoriaId)
                .tenantId(tenantId)
                .status(ConvocatoriaStatus.CLOSED)
                .build();
        when(candidateRepository.findByToken(candidateToken)).thenReturn(Optional.of(invitedCandidate));
        when(convocatoriaRepository.findByIdAndTenantId(convocatoriaId, tenantId))
                .thenReturn(Optional.of(closed));

        var command = new SubmitCandidateResponseCommand(candidateToken, null, List.of());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void requiredVisibleQuestionWithoutAnswer_throwsBadRequest() {
        when(candidateRepository.findByToken(candidateToken)).thenReturn(Optional.of(invitedCandidate));
        when(convocatoriaRepository.findByIdAndTenantId(convocatoriaId, tenantId))
                .thenReturn(Optional.of(activeConvocatoria));
        when(formLoader.loadPublicOrThrow(formId)).thenReturn(activeForm);
        when(conditionalLogicEvaluator.isVisible(any(), any(Map.class))).thenReturn(true);

        var command = new SubmitCandidateResponseCommand(candidateToken, null, List.of());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(be.getMessageKey()).isEqualTo("error.response.required_question_empty");
                });
    }
}
