package com.kodelabs.formflow.modules.forms.application.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.SingleConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionOperator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SingleTypeHandler implements QuestionTypeHandler<SingleConfig> {

    public static final QuestionType QUESTION_TYPE = new QuestionType("SINGLE");

    private final ObjectMapper objectMapper;

    @Override
    public QuestionType type() {
        return QUESTION_TYPE;
    }

    @Override
    public SingleConfig build(Map<String, Object> raw) {
        SingleConfig config = objectMapper.convertValue(raw, SingleConfig.class);
        validateIfNeeded(config);
        return config;
    }

    @Override
    @SneakyThrows
    public SingleConfig deserialize(String json) {
        return objectMapper.readValue(json, SingleConfig.class);
    }

    @Override
    public Map<String, Object> defaultSchema() {
        return Map.of(
                "options", List.of(Map.of("id", "uuid", "label", "Option", "score", 0)),
                "randomize", false);
    }

    @Override
    public Set<ConditionOperator> supportedOperators() {
        return Set.of(ConditionOperator.EQUALS, ConditionOperator.NOT_EQUALS);
    }
}
