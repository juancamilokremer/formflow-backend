package com.kodelabs.formflow.modules.forms.application.usecase.form;

import com.kodelabs.formflow.modules.forms.application.service.stats.QuestionStatsRegistry;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetFormStatsUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetFormStatsQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormStatsResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionStatsResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetFormStatsService implements GetFormStatsUseCase {

    private final FormRepositoryPort formRepository;
    private final FormResponseRepositoryPort responseRepository;
    private final QuestionStatsRegistry statsRegistry;

    @Override
    @Transactional(readOnly = true)
    public FormStatsResult execute(GetFormStatsQuery query) {
        Form form = loadFormWithQuestions(query.formId(), query.tenantId());
        List<FormResponse> responses = loadAllResponses(query.formId(), query.tenantId());
        Map<UUID, List<Object>> answersByQuestion = groupAnswersByQuestion(responses);
        List<QuestionStatsResult> questionStats = computeStatsPerQuestion(form, responses.size(), answersByQuestion);
        return new FormStatsResult(form.getId(), form.getName(), responses.size(), questionStats);
    }

    private Form loadFormWithQuestions(UUID formId, UUID tenantId) {
        return formRepository.findByIdAndTenantIdWithSections(formId, tenantId)
                .orElseThrow(() -> new BusinessException(
                        "error.form.not_found", HttpStatus.NOT_FOUND, formId));
    }

    private List<FormResponse> loadAllResponses(UUID formId, UUID tenantId) {
        return responseRepository.findAllByFormIdAndTenantId(formId, tenantId);
    }

    private Map<UUID, List<Object>> groupAnswersByQuestion(List<FormResponse> responses) {
        Map<UUID, List<Object>> index = new HashMap<>();
        for (FormResponse response : responses) {
            for (var answer : response.getAnswers()) {
                if (answer.getValue() != null) {
                    index.computeIfAbsent(answer.getQuestionId(), k -> new ArrayList<>())
                            .add(answer.getValue());
                }
            }
        }
        return index;
    }

    private List<QuestionStatsResult> computeStatsPerQuestion(
            Form form, int totalResponses, Map<UUID, List<Object>> answersByQuestion) {
        return extractQuestionsInOrder(form).stream()
                .filter(q -> q.getType() != null)
                .flatMap(q -> statsRegistry.find(q.getType())
                        .map(calc -> calc.calculate(
                                q, totalResponses,
                                answersByQuestion.getOrDefault(q.getId(), List.of())))
                        .stream())
                .toList();
    }

    private List<FormQuestion> extractQuestionsInOrder(Form form) {
        if (form.getSections() == null) return List.of();
        return form.getSections().stream()
                .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                .map(FormSection::getQuestions)
                .filter(qs -> qs != null)
                .flatMap(List::stream)
                .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                .toList();
    }
}
