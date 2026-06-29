package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CandidateScoringService {

    private final AnswerScoreExtractor scoreExtractor;
    private final ScoringEngine scoringEngine;

    public ScoringResult compute(Form form, Convocatoria convocatoria, Map<UUID, Object> answerMap) {
        List<FormQuestion> questions = form.getSections().stream()
                .flatMap(s -> s.getQuestions().stream())
                .toList();
        Map<UUID, Integer> obtainedScores = new HashMap<>();
        questions.forEach(q -> obtainedScores.put(q.getId(), scoreExtractor.extractScore(q, answerMap.get(q.getId()))));
        Map<UUID, Double> categoryWeights = convocatoria.getCategoryWeights().stream()
                .collect(Collectors.toMap(CategoryWeight::categoryId, cw -> (double) cw.weight()));
        return scoringEngine.calculate(new ScoringInput(questions, obtainedScores, categoryWeights));
    }
}
