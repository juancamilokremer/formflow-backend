package com.kodelabs.formflow.modules.forms.application.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.TextConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionOperator;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TextTypeHandler implements QuestionTypeHandler<TextConfig> {

    public static final QuestionType QUESTION_TYPE = new QuestionType("TEXT");

    private final ObjectMapper objectMapper;

    @Override
    public QuestionType type() {
        return QUESTION_TYPE;
    }

    @Override
    public TextConfig build(Map<String, Object> raw) {
        TextConfig config = objectMapper.convertValue(raw, TextConfig.class);
        validateIfNeeded(config);
        return config;
    }

    @Override
    @SneakyThrows
    public TextConfig deserialize(String json) {
        return objectMapper.readValue(json, TextConfig.class);
    }

    @Override
    public Map<String, Object> defaultSchema() {
        return Map.of("maxLength", 2000, "placeholder", "", "rows", 1);
    }

    @Override
    public Set<ConditionOperator> supportedOperators() {
        return Set.of(ConditionOperator.IS_ANSWERED, ConditionOperator.IS_EMPTY,
                ConditionOperator.CONTAINS);
    }
}
