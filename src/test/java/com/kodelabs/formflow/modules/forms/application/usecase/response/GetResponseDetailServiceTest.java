package com.kodelabs.formflow.modules.forms.application.usecase.response;

import com.kodelabs.formflow.modules.forms.application.service.ResponseDetailAssembler;
import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateScores;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.FormSnapshot;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetResponseDetailQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.AnswerDetailResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ResponseCategoryScoreResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ResponseDetailResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetResponseDetailServiceTest {

    @Mock private FormLoader formLoader;
    @Mock private FormResponseRepositoryPort responseRepository;
    @Mock private CandidateRepositoryPort candidateRepository;
    @Mock private ResponseDetailAssembler responseDetailAssembler;
    @InjectMocks private GetResponseDetailService service;

    private UUID formId;
    private UUID responseId;
    private UUID tenantId;
    private FormSnapshot snapshot;

    @BeforeEach
    void setUp() {
        formId = UUID.randomUUID();
        responseId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        snapshot = new FormSnapshot(formId, "Form", "CANDIDATES", 1, Instant.now(), List.of());
    }

    @Test
    void happyPath_delegatesToAssemblerAndAssemblesResult() {
        Form form = Form.builder().id(formId).tenantId(tenantId).build();
        FormResponse response = FormResponse.builder()
                .id(responseId).formId(formId).tenantId(tenantId)
                .respondentToken(UUID.randomUUID())
                .formSnapshot(snapshot).answers(List.of())
                .submittedAt(Instant.now())
                .build();
        List<AnswerDetailResult> answers = List.of(
                new AnswerDetailResult(UUID.randomUUID(), "Comentarios", "text", "hola", "hola"));

        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(responseRepository.findByIdAndTenantId(responseId, tenantId)).thenReturn(Optional.of(response));
        when(responseDetailAssembler.resolveScore(null)).thenReturn(null);
        when(responseDetailAssembler.resolveCategoryScores(null, formId, tenantId)).thenReturn(null);
        when(responseDetailAssembler.buildOrderedAnswers(response)).thenReturn(answers);

        ResponseDetailResult result = service.execute(new GetResponseDetailQuery(formId, responseId, tenantId));

        assertThat(result.id()).isEqualTo(responseId);
        assertThat(result.formSnapshot()).isEqualTo(snapshot);
        assertThat(result.answers()).isEqualTo(answers);
        assertThat(result.totalScore()).isNull();
        assertThat(result.categoryScores()).isNull();
    }

    @Test
    void candidateResponse_loadsCandidateAndDelegatesScoreResolution() {
        UUID candidateId = UUID.randomUUID();
        Form form = Form.builder().id(formId).tenantId(tenantId).build();
        FormResponse response = FormResponse.builder()
                .id(responseId).formId(formId).tenantId(tenantId)
                .respondentToken(UUID.randomUUID()).candidateId(candidateId)
                .formSnapshot(snapshot).answers(List.of())
                .submittedAt(Instant.now())
                .build();
        Candidate candidate = Candidate.builder().id(candidateId)
                .scores(new CandidateScores(88.5, List.of())).build();
        List<ResponseCategoryScoreResult> categoryScores = List.of(
                new ResponseCategoryScoreResult(UUID.randomUUID(), "Técnicas", 32.0));

        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(responseRepository.findByIdAndTenantId(responseId, tenantId)).thenReturn(Optional.of(response));
        when(candidateRepository.findAllByIds(List.of(candidateId))).thenReturn(List.of(candidate));
        when(responseDetailAssembler.resolveScore(candidate)).thenReturn(88.5);
        when(responseDetailAssembler.resolveCategoryScores(candidate, formId, tenantId)).thenReturn(categoryScores);
        when(responseDetailAssembler.buildOrderedAnswers(response)).thenReturn(List.of());

        ResponseDetailResult result = service.execute(new GetResponseDetailQuery(formId, responseId, tenantId));

        assertThat(result.totalScore()).isEqualTo(88.5);
        assertThat(result.categoryScores()).isEqualTo(categoryScores);
    }

    @Test
    void formBelongsToOtherTenant_throwsNotFound() {
        when(formLoader.loadOrThrow(formId, tenantId)).thenThrow(new BusinessException("error.form.not_found", HttpStatus.NOT_FOUND, formId));

        assertThatThrownBy(() -> service.execute(new GetResponseDetailQuery(formId, responseId, tenantId)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void responseBelongsToOtherForm_throwsNotFound() {
        UUID otherFormId = UUID.randomUUID();
        Form form = Form.builder().id(formId).tenantId(tenantId).build();
        FormSnapshot otherSnapshot = new FormSnapshot(otherFormId, "Other", "CANDIDATES", 1, Instant.now(), List.of());
        FormResponse response = FormResponse.builder()
                .id(responseId).formId(otherFormId).tenantId(tenantId)
                .respondentToken(UUID.randomUUID()).formSnapshot(otherSnapshot)
                .answers(List.of()).submittedAt(Instant.now())
                .build();

        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(responseRepository.findByIdAndTenantId(responseId, tenantId)).thenReturn(Optional.of(response));

        assertThatThrownBy(() -> service.execute(new GetResponseDetailQuery(formId, responseId, tenantId)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void responseNotFound_throwsNotFound() {
        Form form = Form.builder().id(formId).tenantId(tenantId).build();
        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(responseRepository.findByIdAndTenantId(responseId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new GetResponseDetailQuery(formId, responseId, tenantId)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
