package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.application.service.stats.QuestionStatsRegistry;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionStatsResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Shared aggregation engine behind "stats por pregunta": used by GetFormStatsService (single form)
 * and GetConvocatoriaQuestionStatsService (form scoped to a convocatoria) so both go through the
 * same QuestionStatsCalculator pipeline instead of duplicating it.
 */
@Component
@RequiredArgsConstructor
public class QuestionStatsComputer {

    private final QuestionStatsRegistry statsRegistry;

    public List<QuestionStatsResult> compute(Form form, int totalResponses, List<FormResponse> responses) {
        Map<UUID, List<Object>> answersByQuestion = groupAnswersByQuestion(responses);
        return extractQuestionsInOrder(form).stream()
                .filter(q -> q.getType() != null)
                .flatMap(q -> statsRegistry.find(q.getType())
                        .map(calc -> calc.calculate(
                                q, totalResponses,
                                answersByQuestion.getOrDefault(q.getId(), List.of())))
                        .stream())
                .toList();
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

    private List<FormQuestion> extractQuestionsInOrder(Form form) {
        if (form.getSections() == null) return List.of();
        // Sort questions WITHIN each section before flattening — a global sort of the flattened
        // list would compare `position` values that are only meaningful relative to their own
        // section, interleaving questions from different sections that happen to share a position.
        return form.getSections().stream()
                .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                .flatMap(section -> section.getQuestions() == null
                        ? Stream.<FormQuestion>empty()
                        : section.getQuestions().stream()
                                .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition())))
                .toList();
    }
}
