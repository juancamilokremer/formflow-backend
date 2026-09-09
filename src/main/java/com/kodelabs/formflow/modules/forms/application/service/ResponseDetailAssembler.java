package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.AnswerValue;
import com.kodelabs.formflow.modules.forms.domain.model.Category;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateFormScore;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.QuestionSnapshot;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.SectionSnapshot;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.AnswerDetailResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ResponseCategoryScoreResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Shared logic behind "detalle de respuesta": resolves a candidate's already-computed score/category
 * scores and builds the ordered answer list from a response's frozen form_snapshot. Used by
 * GetResponseDetailService (single form/response) and ExportCandidateResponsePdfService (all of a
 * candidate's responses across a convocatoria's forms) so both go through the same logic instead of
 * duplicating it.
 */
@Component
@RequiredArgsConstructor
public class ResponseDetailAssembler {

    private static final String INFO_QUESTION_TYPE = "info";

    private final CategoryRepositoryPort categoryRepository;
    private final AnswerDisplayFormatter answerDisplayFormatter;

    public Double resolveScore(Candidate candidate) {
        if (candidate == null || candidate.getScores() == null) return null;
        return candidate.getScores().total();
    }

    public List<ResponseCategoryScoreResult> resolveCategoryScores(Candidate candidate, UUID formId, UUID tenantId) {
        CandidateFormScore formScore = findFormScore(candidate, formId);
        if (formScore == null || formScore.byCategory() == null || formScore.byCategory().isEmpty()) return null;

        Map<UUID, String> categoryNames = categoryRepository
                .findAllByIdsAndTenantId(List.copyOf(formScore.byCategory().keySet()), tenantId).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        return formScore.byCategory().entrySet().stream()
                .map(e -> new ResponseCategoryScoreResult(
                        e.getKey(), categoryNames.getOrDefault(e.getKey(), e.getKey().toString()), e.getValue()))
                .toList();
    }

    private CandidateFormScore findFormScore(Candidate candidate, UUID formId) {
        if (candidate == null || candidate.getScores() == null || candidate.getScores().perForm() == null) return null;
        return candidate.getScores().perForm().stream()
                .filter(fs -> formId.equals(fs.formId()))
                .findFirst()
                .orElse(null);
    }

    public List<AnswerDetailResult> buildOrderedAnswers(FormResponse response) {
        Map<UUID, Object> valuesByQuestion = response.getAnswers().stream()
                .collect(Collectors.toMap(AnswerValue::getQuestionId, AnswerValue::getValue));

        return response.getFormSnapshot().sections().stream()
                .sorted(Comparator.comparingInt(SectionSnapshot::position))
                .flatMap(section -> section.questions().stream()
                        .sorted(Comparator.comparingInt(QuestionSnapshot::position)))
                .filter(question -> !INFO_QUESTION_TYPE.equals(question.type()))
                .map(question -> toAnswerDetailResult(question, valuesByQuestion.get(question.id())))
                .toList();
    }

    private AnswerDetailResult toAnswerDetailResult(QuestionSnapshot question, Object value) {
        String displayValue = answerDisplayFormatter.format(question, value);
        return new AnswerDetailResult(question.id(), question.title(), question.type(), value, displayValue);
    }
}
