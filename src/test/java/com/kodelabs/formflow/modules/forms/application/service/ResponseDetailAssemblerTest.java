package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.AnswerValue;
import com.kodelabs.formflow.modules.forms.domain.model.Category;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateFormScore;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateScores;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.FormSnapshot;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.QuestionSnapshot;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.SectionSnapshot;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.AnswerDetailResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ResponseCategoryScoreResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResponseDetailAssemblerTest {

    @Mock private CategoryRepositoryPort categoryRepository;
    @Spy private AnswerDisplayFormatter answerDisplayFormatter = new AnswerDisplayFormatter();
    @InjectMocks private ResponseDetailAssembler assembler;

    private final UUID formId = UUID.randomUUID();
    private final UUID tenantId = UUID.randomUUID();

    private QuestionSnapshot textQuestion(UUID id, int position) {
        return new QuestionSnapshot(id, "Comentarios", null, "text", position, false, null, null, Map.of());
    }

    @Test
    void resolveScore_isNullWhenCandidateIsNull() {
        assertThat(assembler.resolveScore(null)).isNull();
    }

    @Test
    void resolveScore_isNullWhenCandidateHasNoScores() {
        Candidate candidate = Candidate.builder().id(UUID.randomUUID()).build();
        assertThat(assembler.resolveScore(candidate)).isNull();
    }

    @Test
    void resolveScore_returnsCandidateTotal() {
        Candidate candidate = Candidate.builder().id(UUID.randomUUID())
                .scores(new CandidateScores(88.5, List.of())).build();
        assertThat(assembler.resolveScore(candidate)).isEqualTo(88.5);
    }

    @Test
    void resolveCategoryScores_includesResolvedCategoryNames() {
        UUID candidateId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        CandidateFormScore formScore = new CandidateFormScore(UUID.randomUUID(), formId, 80.0, Map.of(categoryId, 32.0));
        Candidate candidate = Candidate.builder().id(candidateId)
                .scores(new CandidateScores(80.0, List.of(formScore))).build();
        Category category = Category.builder().id(categoryId).name("Competencias Técnicas").build();

        when(categoryRepository.findAllByIdsAndTenantId(List.of(categoryId), tenantId)).thenReturn(List.of(category));

        List<ResponseCategoryScoreResult> result = assembler.resolveCategoryScores(candidate, formId, tenantId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).categoryId()).isEqualTo(categoryId);
        assertThat(result.get(0).categoryName()).isEqualTo("Competencias Técnicas");
        assertThat(result.get(0).score()).isEqualTo(32.0);
    }

    @Test
    void resolveCategoryScores_forDifferentForm_returnsNull() {
        UUID otherFormId = UUID.randomUUID();
        CandidateFormScore formScore = new CandidateFormScore(
                UUID.randomUUID(), otherFormId, 80.0, Map.of(UUID.randomUUID(), 32.0));
        Candidate candidate = Candidate.builder().id(UUID.randomUUID())
                .scores(new CandidateScores(80.0, List.of(formScore))).build();

        assertThat(assembler.resolveCategoryScores(candidate, formId, tenantId)).isNull();
    }

    @Test
    void resolveCategoryScores_isNullWhenCandidateIsNull() {
        assertThat(assembler.resolveCategoryScores(null, formId, tenantId)).isNull();
    }

    @Test
    void buildOrderedAnswers_ordersBySnapshotSectionAndQuestionPosition() {
        UUID q1 = UUID.randomUUID();
        UUID q2 = UUID.randomUUID();
        UUID q3 = UUID.randomUUID();
        SectionSnapshot sectionA = new SectionSnapshot(UUID.randomUUID(), "A", null, 0, null,
                List.of(textQuestion(q2, 1), textQuestion(q1, 0)));
        SectionSnapshot sectionB = new SectionSnapshot(UUID.randomUUID(), "B", null, 1, null,
                List.of(textQuestion(q3, 0)));
        FormSnapshot snapshot = new FormSnapshot(formId, "Form", "REGISTRATION", 1, Instant.now(),
                List.of(sectionB, sectionA));
        FormResponse response = FormResponse.builder()
                .id(UUID.randomUUID()).formId(formId).tenantId(tenantId)
                .respondentToken(UUID.randomUUID())
                .formSnapshot(snapshot)
                .answers(List.of(
                        AnswerValue.builder().questionId(q3).value("c").build(),
                        AnswerValue.builder().questionId(q1).value("a").build(),
                        AnswerValue.builder().questionId(q2).value("b").build()))
                .submittedAt(Instant.now())
                .build();

        List<AnswerDetailResult> answers = assembler.buildOrderedAnswers(response);

        assertThat(answers).extracting("questionId").containsExactly(q1, q2, q3);
    }

    @Test
    void buildOrderedAnswers_skipsInfoQuestionsAndFillsMissingAnswersAsNull() {
        UUID infoId = UUID.randomUUID();
        UUID unansweredId = UUID.randomUUID();
        QuestionSnapshot infoQuestion = new QuestionSnapshot(infoId, "Bienvenida", null, "info", 0, false, null, null, Map.of());
        QuestionSnapshot unanswered = new QuestionSnapshot(unansweredId, "Opcional", null, "text", 1, false, null, null, Map.of());
        SectionSnapshot section = new SectionSnapshot(UUID.randomUUID(), "Sección", null, 0, null,
                List.of(infoQuestion, unanswered));
        FormSnapshot snapshot = new FormSnapshot(formId, "Form", "REGISTRATION", 1, Instant.now(), List.of(section));
        FormResponse response = FormResponse.builder()
                .id(UUID.randomUUID()).formId(formId).tenantId(tenantId)
                .respondentToken(UUID.randomUUID())
                .formSnapshot(snapshot)
                .answers(List.of())
                .submittedAt(Instant.now())
                .build();

        List<AnswerDetailResult> answers = assembler.buildOrderedAnswers(response);

        assertThat(answers).hasSize(1);
        assertThat(answers.get(0).questionId()).isEqualTo(unansweredId);
        assertThat(answers.get(0).displayValue()).isNull();
    }
}
