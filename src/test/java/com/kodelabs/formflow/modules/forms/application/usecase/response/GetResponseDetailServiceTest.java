package com.kodelabs.formflow.modules.forms.application.usecase.response;

import com.kodelabs.formflow.modules.forms.application.service.AnswerDisplayFormatter;
import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.domain.model.AnswerValue;
import com.kodelabs.formflow.modules.forms.domain.model.Category;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateFormScore;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateScores;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.FormSnapshot;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.QuestionSnapshot;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.SectionSnapshot;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetResponseDetailQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ResponseDetailResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
    @Mock private CategoryRepositoryPort categoryRepository;
    @Spy private AnswerDisplayFormatter answerDisplayFormatter = new AnswerDisplayFormatter();
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

    private QuestionSnapshot textQuestion(UUID id, int position) {
        return new QuestionSnapshot(id, "Comentarios", null, "text", position, false, null, null, Map.of());
    }

    @Test
    void happyPath_returnsDetailWithAnswersAndSnapshot() {
        Form form = Form.builder().id(formId).tenantId(tenantId).build();
        SectionSnapshot section = new SectionSnapshot(UUID.randomUUID(), "Sección", null, 0, null,
                List.of(textQuestion(questionId, 0)));
        FormSnapshot snapshot = new FormSnapshot(formId, "Form", "CANDIDATES", 1, Instant.now(), List.of(section));
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
        assertThat(result.answers().get(0).questionTitle()).isEqualTo("Comentarios");
        assertThat(result.answers().get(0).displayValue()).isEqualTo("opt-1");
        assertThat(result.totalScore()).isNull();
        assertThat(result.categoryScores()).isNull();
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
                .scores(new CandidateScores(88.5, List.of()))
                .build();

        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(responseRepository.findByIdAndTenantId(responseId, tenantId)).thenReturn(Optional.of(response));
        when(candidateRepository.findAllByIds(List.of(candidateId))).thenReturn(List.of(candidate));

        ResponseDetailResult result = service.execute(new GetResponseDetailQuery(formId, responseId, tenantId));

        assertThat(result.totalScore()).isEqualTo(88.5);
        assertThat(result.categoryScores()).isNull();
    }

    @Test
    void ordersAnswersBySnapshotSectionAndQuestionPosition() {
        UUID q1 = UUID.randomUUID();
        UUID q2 = UUID.randomUUID();
        UUID q3 = UUID.randomUUID();
        Form form = Form.builder().id(formId).tenantId(tenantId).build();
        SectionSnapshot sectionA = new SectionSnapshot(UUID.randomUUID(), "A", null, 0, null,
                List.of(textQuestion(q2, 1), textQuestion(q1, 0)));
        SectionSnapshot sectionB = new SectionSnapshot(UUID.randomUUID(), "B", null, 1, null,
                List.of(textQuestion(q3, 0)));
        FormSnapshot snapshot = new FormSnapshot(formId, "Form", "REGISTRATION", 1, Instant.now(),
                List.of(sectionB, sectionA));
        FormResponse response = FormResponse.builder()
                .id(responseId).formId(formId).tenantId(tenantId)
                .respondentToken(UUID.randomUUID())
                .formSnapshot(snapshot)
                .answers(List.of(
                        AnswerValue.builder().questionId(q3).value("c").build(),
                        AnswerValue.builder().questionId(q1).value("a").build(),
                        AnswerValue.builder().questionId(q2).value("b").build()))
                .submittedAt(Instant.now())
                .build();

        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(responseRepository.findByIdAndTenantId(responseId, tenantId)).thenReturn(Optional.of(response));

        ResponseDetailResult result = service.execute(new GetResponseDetailQuery(formId, responseId, tenantId));

        assertThat(result.answers()).extracting("questionId").containsExactly(q1, q2, q3);
    }

    @Test
    void skipsInfoQuestionsAndFillsMissingAnswersAsNull() {
        UUID infoId = UUID.randomUUID();
        UUID unansweredId = UUID.randomUUID();
        Form form = Form.builder().id(formId).tenantId(tenantId).build();
        QuestionSnapshot infoQuestion = new QuestionSnapshot(infoId, "Bienvenida", null, "info", 0, false, null, null, Map.of());
        QuestionSnapshot unanswered = new QuestionSnapshot(unansweredId, "Opcional", null, "text", 1, false, null, null, Map.of());
        SectionSnapshot section = new SectionSnapshot(UUID.randomUUID(), "Sección", null, 0, null,
                List.of(infoQuestion, unanswered));
        FormSnapshot snapshot = new FormSnapshot(formId, "Form", "REGISTRATION", 1, Instant.now(), List.of(section));
        FormResponse response = FormResponse.builder()
                .id(responseId).formId(formId).tenantId(tenantId)
                .respondentToken(UUID.randomUUID())
                .formSnapshot(snapshot)
                .answers(List.of())
                .submittedAt(Instant.now())
                .build();

        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(responseRepository.findByIdAndTenantId(responseId, tenantId)).thenReturn(Optional.of(response));

        ResponseDetailResult result = service.execute(new GetResponseDetailQuery(formId, responseId, tenantId));

        assertThat(result.answers()).hasSize(1);
        assertThat(result.answers().get(0).questionId()).isEqualTo(unansweredId);
        assertThat(result.answers().get(0).displayValue()).isNull();
    }

    @Test
    void candidateFormScore_includesCategoryScoresWithResolvedNames() {
        UUID candidateId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Form form = Form.builder().id(formId).tenantId(tenantId).build();
        FormSnapshot snapshot = new FormSnapshot(formId, "Form", "CANDIDATES", 1, Instant.now(), List.of());
        FormResponse response = FormResponse.builder()
                .id(responseId).formId(formId).tenantId(tenantId)
                .respondentToken(UUID.randomUUID()).candidateId(candidateId)
                .formSnapshot(snapshot).answers(List.of())
                .submittedAt(Instant.now())
                .build();
        CandidateFormScore formScore = new CandidateFormScore(
                UUID.randomUUID(), formId, 80.0, Map.of(categoryId, 32.0));
        Candidate candidate = Candidate.builder()
                .id(candidateId)
                .scores(new CandidateScores(80.0, List.of(formScore)))
                .build();
        Category category = Category.builder().id(categoryId).name("Competencias Técnicas").build();

        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(responseRepository.findByIdAndTenantId(responseId, tenantId)).thenReturn(Optional.of(response));
        when(candidateRepository.findAllByIds(List.of(candidateId))).thenReturn(List.of(candidate));
        when(categoryRepository.findAllByIdsAndTenantId(List.of(categoryId), tenantId)).thenReturn(List.of(category));

        ResponseDetailResult result = service.execute(new GetResponseDetailQuery(formId, responseId, tenantId));

        assertThat(result.categoryScores()).hasSize(1);
        assertThat(result.categoryScores().get(0).categoryId()).isEqualTo(categoryId);
        assertThat(result.categoryScores().get(0).categoryName()).isEqualTo("Competencias Técnicas");
        assertThat(result.categoryScores().get(0).score()).isEqualTo(32.0);
    }

    @Test
    void candidateFormScore_forDifferentForm_returnsNullCategoryScores() {
        UUID candidateId = UUID.randomUUID();
        UUID otherFormId = UUID.randomUUID();
        Form form = Form.builder().id(formId).tenantId(tenantId).build();
        FormSnapshot snapshot = new FormSnapshot(formId, "Form", "CANDIDATES", 1, Instant.now(), List.of());
        FormResponse response = FormResponse.builder()
                .id(responseId).formId(formId).tenantId(tenantId)
                .respondentToken(UUID.randomUUID()).candidateId(candidateId)
                .formSnapshot(snapshot).answers(List.of())
                .submittedAt(Instant.now())
                .build();
        CandidateFormScore formScore = new CandidateFormScore(
                UUID.randomUUID(), otherFormId, 80.0, Map.of(UUID.randomUUID(), 32.0));
        Candidate candidate = Candidate.builder()
                .id(candidateId)
                .scores(new CandidateScores(80.0, List.of(formScore)))
                .build();

        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(responseRepository.findByIdAndTenantId(responseId, tenantId)).thenReturn(Optional.of(response));
        when(candidateRepository.findAllByIds(List.of(candidateId))).thenReturn(List.of(candidate));

        ResponseDetailResult result = service.execute(new GetResponseDetailQuery(formId, responseId, tenantId));

        assertThat(result.categoryScores()).isNull();
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
