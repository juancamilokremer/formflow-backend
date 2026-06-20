package com.kodelabs.formflow.modules.forms.application.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.MultipleConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionOperator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MultipleTypeHandler implements QuestionTypeHandler<MultipleConfig> {

    public static final QuestionType QUESTION_TYPE = new QuestionType("MULTIPLE");

    private final ObjectMapper objectMapper;

    @Override
    public QuestionType type() {
        return QUESTION_TYPE;
    }

    @Override
    public MultipleConfig build(Map<String, Object> raw) {
        MultipleConfig config = objectMapper.convertValue(raw, MultipleConfig.class);
        validateIfNeeded(config);
        return config;
    }

    @Override
    @SneakyThrows
    public MultipleConfig deserialize(String json) {
        return objectMapper.readValue(json, MultipleConfig.class);
    }

    @Override
    public Map<String, Object> defaultSchema() {
        return Map.of(
                "options", List.of(Map.of("id", "uuid", "label", "Option", "score", 0)),
                "randomize", false);
    }

    @Override
    public Set<ConditionOperator> supportedOperators() {
        return Set.of(ConditionOperator.CONTAINS, ConditionOperator.NOT_CONTAINS);
    }
}
