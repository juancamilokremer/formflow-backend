package com.kodelabs.formflow.modules.forms.application.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.MatrixConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MatrixTypeHandler implements QuestionTypeHandler<MatrixConfig> {

    public static final QuestionType QUESTION_TYPE = new QuestionType("MATRIX");

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
                "columns", List.of(Map.of("id", "uuid", "label", "Column", "score", 0)));
    }
}
