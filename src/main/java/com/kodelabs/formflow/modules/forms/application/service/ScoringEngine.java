package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Stateless scoring engine.
 * Formula per category:
 *   contribution = (obtained / maxPossible) * weight
 * Total score = sum of all category contributions (0–100).
 * Questions with no category or no weight assignment are excluded.
 */
@Component
public class ScoringEngine {

    public ScoringResult calculate(ScoringInput input) {
        Map<UUID, List<FormQuestion>> byCategory = input.questions().stream()
                .filter(q -> q.getCategoryId() != null)
                .filter(q -> input.categoryWeights().containsKey(q.getCategoryId()))
                .collect(Collectors.groupingBy(FormQuestion::getCategoryId));

        Map<UUID, CategoryScore> scoresByCategory = new LinkedHashMap<>();
        double total = 0.0;

        for (Map.Entry<UUID, Double> entry : input.categoryWeights().entrySet()) {
            UUID catId = entry.getKey();
            double weight = entry.getValue();

            List<FormQuestion> questions = byCategory.getOrDefault(catId, List.of());

            int maxPossible = questions.stream()
                    .mapToInt(q -> q.getConfig() != null ? q.getConfig().maxScore() : 0)
                    .sum();

            int obtained = questions.stream()
                    .mapToInt(q -> input.obtainedScores().getOrDefault(q.getId(), 0))
                    .sum();

            double contribution = maxPossible > 0 ? ((double) obtained / maxPossible) * weight : 0.0;
            total += contribution;

            scoresByCategory.put(catId, new CategoryScore(obtained, maxPossible, weight, contribution));
        }

        return new ScoringResult(Math.min(total, 100.0), Map.copyOf(scoresByCategory));
    }
}
