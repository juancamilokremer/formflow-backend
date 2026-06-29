package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.config.MultipleConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.ScaleConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.SingleConfig;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Extracts the score a respondent obtained for a single question based on their answer.
 * Only SINGLE, MULTIPLE, and SCALE question types contribute to scoring; all others return 0.
 * Matching is done by option id for choice types and by numeric value for scale types.
 */
@Component
public class AnswerScoreExtractor {

    public int extractScore(FormQuestion question, Object value) {
        if (question.getConfig() == null || value == null) return 0;
        var config = question.getConfig();
        if (config instanceof SingleConfig sc) return extractFromSingle(sc, value);
        if (config instanceof MultipleConfig mc) return extractFromMultiple(mc, value);
        if (config instanceof ScaleConfig sc) return extractFromScale(sc, value);
        return 0;
    }

    private int extractFromSingle(SingleConfig config, Object value) {
        String optionId = String.valueOf(value);
        return config.getOptions().stream()
                .filter(o -> optionId.equals(o.getId()))
                .mapToInt(com.kodelabs.formflow.modules.forms.domain.model.config.AnswerOption::getScore)
                .findFirst()
                .orElse(0);
    }

    private int extractFromMultiple(MultipleConfig config, Object value) {
        List<String> selectedIds = toStringList(value);
        return config.getOptions().stream()
                .filter(o -> selectedIds.contains(o.getId()))
                .mapToInt(com.kodelabs.formflow.modules.forms.domain.model.config.AnswerOption::getScore)
                .sum();
    }

    private int extractFromScale(ScaleConfig config, Object value) {
        int scaleValue = toInt(value);
        return config.getOptions().stream()
                .filter(o -> o.getValue() == scaleValue)
                .mapToInt(com.kodelabs.formflow.modules.forms.domain.model.config.ScaleOption::getScore)
                .findFirst()
                .orElse(0);
    }

    @SuppressWarnings("unchecked")
    private List<String> toStringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of(String.valueOf(value));
    }

    private int toInt(Object value) {
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
