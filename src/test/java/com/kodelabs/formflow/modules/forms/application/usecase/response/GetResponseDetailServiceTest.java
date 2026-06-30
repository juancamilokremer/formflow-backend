package com.kodelabs.formflow.modules.forms.application.usecase.response;

import com.kodelabs.formflow.modules.forms.domain.model.AnswerValue;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateScores;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.FormSnapshot;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetResponseDetailQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ResponseDetailResult;
import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
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
import java.util.Map;
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
    @InjectMocks private GetResponseDetailService service;

    private UUID formId;
    private UUID responseId;
    private UUID tenantId;
    private UUID questionId;

    @BeforeEach
    void setUp() {
        formId = UUID.randomUUID();
        responseId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        questionId = UUID.randomUUID();
    }

    @Test
    void happyPath_returnsDetailWithAnswersAndSnapshot() {
        Form form = Form.builder().id(formId).tenantId(tenantId).build();
        FormSnapshot snapshot = new FormSnapshot(formId, "Form", "CANDIDATES", 1, Instant.now(), List.of());
        AnswerValue answer = AnswerValue.builder().questionId(questionId).value("opt-1").build();
        FormResponse response = FormResponse.builder()
                .id(responseId).formId(formId).tenantId(tenantId)
                .respondentToken(UUID.randomUUID())
                .formSnapshot(snapshot)
                .answers(List.of(answer))
                .submittedAt(Instant.now())
                .build();

        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(responseRepository.findByIdAndTenantId(responseId, tenantId)).thenReturn(Optional.of(response));

        ResponseDetailResult result = service.execute(new GetResponseDetailQuery(formId, responseId, tenantId));

        assertThat(result.id()).isEqualTo(responseId);
        assertThat(result.formSnapshot()).isEqualTo(snapshot);
        assertThat(result.answers()).hasSize(1);
        assertThat(result.answers().get(0).questionId()).isEqualTo(questionId);
        assertThat(result.totalScore()).isNull();
    }

    @Test
    void candidateResponse_includesScoreFromCandidate() {
        UUID candidateId = UUID.randomUUID();
        Form form = Form.builder().id(formId).tenantId(tenantId).build();
        FormSnapshot snapshot = new FormSnapshot(formId, "Form", "CANDIDATES", 1, Instant.now(), List.of());
        FormResponse response = FormResponse.builder()
                .id(responseId).formId(formId).tenantId(tenantId)
                .respondentToken(UUID.randomUUID()).candidateId(candidateId)
                .formSnapshot(snapshot).answers(List.of())
                .submittedAt(Instant.now())
                .build();
        Candidate candidate = Candidate.builder()
                .id(candidateId)
                .scores(new CandidateScores(88.5, Map.of()))
                .build();

        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(responseRepository.findByIdAndTenantId(responseId, tenantId)).thenReturn(Optional.of(response));
        when(candidateRepository.findAllByIds(List.of(candidateId))).thenReturn(List.of(candidate));

        ResponseDetailResult result = service.execute(new GetResponseDetailQuery(formId, responseId, tenantId));

        assertThat(result.totalScore()).isEqualTo(88.5);
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
        FormSnapshot snapshot = new FormSnapshot(otherFormId, "Other", "CANDIDATES", 1, Instant.now(), List.of());
        FormResponse response = FormResponse.builder()
                .id(responseId).formId(otherFormId).tenantId(tenantId)
                .respondentToken(UUID.randomUUID()).formSnapshot(snapshot)
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
