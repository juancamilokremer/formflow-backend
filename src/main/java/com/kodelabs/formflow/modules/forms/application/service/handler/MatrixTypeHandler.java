package com.kodelabs.formflow.modules.forms.application.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.MatrixColumn;
import com.kodelabs.formflow.modules.forms.domain.model.config.MatrixConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.QuestionConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionOperator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MatrixTypeHandler implements QuestionTypeHandler<MatrixConfig> {

    public static final QuestionType QUESTION_TYPE = QuestionType.MATRIX;

    private final ObjectMapper objectMapper;

    @Override
    public QuestionType type() {
        return QUESTION_TYPE;
    }

    @Override
    public MatrixConfig build(Map<String, Object> raw) {
        MatrixConfig config = objectMapper.convertValue(raw, MatrixConfig.class);
        validateIfNeeded(config);
        return config;
    }

    @Override
    @SneakyThrows
    public MatrixConfig deserialize(String json) {
        return objectMapper.readValue(json, MatrixConfig.class);
    }

    @Override
    public Map<String, Object> defaultSchema() {
        return Map.of(
                "rows", List.of(Map.of("id", "uuid", "label", "Row")),
                "columns", List.of(Map.of("id", "uuid", "label", "Column", "score", 0)),
                "scoringType", "NONE");
    }

    @Override
    public Set<ConditionOperator> supportedOperators() {
        return Set.of();
    }

    @Override
    public QuestionConfig redactForPublic(QuestionConfig config) {
        MatrixConfig c = (MatrixConfig) config;
        return MatrixConfig.builder()
                .rows(c.getRows())
                .columns(c.getColumns().stream()
                        .map(col -> MatrixColumn.builder().id(col.getId()).label(col.getLabel()).build())
                        .toList())
                .scoringType(c.getScoringType())
                .build();
    }
}
