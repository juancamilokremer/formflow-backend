package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.snapshot.QuestionSnapshot;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Resolves an answer's raw stored value into human-readable text, looking up
 * option/row/column labels from the form_snapshot config for choice-based types.
 * Other types (scale, nps, text, date, file) are stringified as-is; richer
 * rendering for those (date formatting, file download links) belongs in the
 * client, which has access to the raw value alongside this display text.
 */
@Component
public class AnswerDisplayFormatter {

    public String format(QuestionSnapshot question, Object value) {
        if (value == null) return null;
        return switch (question.type()) {
            case "single" -> resolveOptionLabel(question, value);
            case "multiple" -> resolveOptionLabels(question, value);
            case "matrix" -> resolveMatrix(question, value);
            default -> String.valueOf(value);
        };
    }

    @SuppressWarnings("unchecked")
    private String resolveOptionLabel(QuestionSnapshot question, Object optionId) {
        List<Map<String, Object>> options = (List<Map<String, Object>>)
                question.config().getOrDefault("options", List.of());
        return options.stream()
                .filter(o -> optionId.equals(o.get("id")))
                .map(o -> String.valueOf(o.get("label")))
                .findFirst()
                .orElse(String.valueOf(optionId));
    }

    private String resolveOptionLabels(QuestionSnapshot question, Object value) {
        List<?> selectedIds = value instanceof List<?> list ? list : List.of(value);
        return selectedIds.stream()
                .map(id -> resolveOptionLabel(question, id))
                .collect(Collectors.joining(", "));
    }

    @SuppressWarnings("unchecked")
    private String resolveMatrix(QuestionSnapshot question, Object value) {
        if (!(value instanceof Map<?, ?> answerMap)) return String.valueOf(value);
        List<Map<String, Object>> rows = (List<Map<String, Object>>)
                question.config().getOrDefault("rows", List.of());
        List<Map<String, Object>> columns = (List<Map<String, Object>>)
                question.config().getOrDefault("columns", List.of());
        return rows.stream()
                .filter(row -> answerMap.get(row.get("id")) != null)
                .map(row -> rowSummary(row, columns, answerMap))
                .collect(Collectors.joining("; "));
    }

    private String rowSummary(Map<String, Object> row, List<Map<String, Object>> columns, Map<?, ?> answerMap) {
        Object columnId = answerMap.get(row.get("id"));
        String columnLabel = columns.stream()
                .filter(c -> columnId.equals(c.get("id")))
                .map(c -> String.valueOf(c.get("label")))
                .findFirst()
                .orElse(String.valueOf(columnId));
        return row.get("label") + ": " + columnLabel;
    }
}
