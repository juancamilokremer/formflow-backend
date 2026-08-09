package com.kodelabs.formflow.modules.forms.application.usecase.response;

import com.kodelabs.formflow.modules.forms.domain.model.AnswerValue;
import com.kodelabs.formflow.modules.forms.domain.model.Category;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateFormScore;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.QuestionSnapshot;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.SectionSnapshot;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetResponseDetailUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetResponseDetailQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.AnswerDetailResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ResponseCategoryScoreResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ResponseDetailResult;
import com.kodelabs.formflow.modules.forms.application.service.AnswerDisplayFormatter;
import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetResponseDetailService implements GetResponseDetailUseCase {

    private static final String INFO_QUESTION_TYPE = "info";

    private final FormLoader formLoader;
    private final FormResponseRepositoryPort responseRepository;
    private final CandidateRepositoryPort candidateRepository;
    private final CategoryRepositoryPort categoryRepository;
    private final AnswerDisplayFormatter answerDisplayFormatter;

    @Override
    @Transactional(readOnly = true)
    public ResponseDetailResult execute(GetResponseDetailQuery query) {
        formLoader.loadOrThrow(query.formId(), query.tenantId());
        FormResponse response = loadResponseInForm(query.responseId(), query.formId(), query.tenantId());
        Candidate candidate = loadCandidate(response.getCandidateId());
        Double totalScore = resolveScore(candidate);
        List<ResponseCategoryScoreResult> categoryScores = resolveCategoryScores(candidate, query.formId(), query.tenantId());
        List<AnswerDetailResult> answers = buildOrderedAnswers(response);
        return new ResponseDetailResult(
                response.getId(), response.getFormId(), response.getRespondentToken(),
                response.getConvocatoriaId(), response.getCandidateId(), totalScore, categoryScores,
                response.getFormSnapshot(), answers, response.getSubmittedAt(), response.getStartedAt());
    }

    private FormResponse loadResponseInForm(UUID responseId, UUID formId, UUID tenantId) {
        FormResponse response = responseRepository.findByIdAndTenantId(responseId, tenantId)
                .orElseThrow(() -> new BusinessException("error.response.not_found", HttpStatus.NOT_FOUND, responseId));
        if (!formId.equals(response.getFormId())) {
            throw new BusinessException("error.response.not_found", HttpStatus.NOT_FOUND, responseId);
        }
        return response;
    }

    private Candidate loadCandidate(UUID candidateId) {
        if (candidateId == null) return null;
        return candidateRepository.findAllByIds(List.of(candidateId)).stream().findFirst().orElse(null);
    }

    private Double resolveScore(Candidate candidate) {
        if (candidate == null || candidate.getScores() == null) return null;
        return candidate.getScores().total();
    }

    private List<ResponseCategoryScoreResult> resolveCategoryScores(Candidate candidate, UUID formId, UUID tenantId) {
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

    private List<AnswerDetailResult> buildOrderedAnswers(FormResponse response) {
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
