package com.kodelabs.formflow.modules.forms.application.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.NpsConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionOperator;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class NpsTypeHandler implements QuestionTypeHandler<NpsConfig> {

    public static final QuestionType QUESTION_TYPE = new QuestionType("NPS");

    private final ObjectMapper objectMapper;

    @Override
    public QuestionType type() {
        return QUESTION_TYPE;
    }

    @Override
    public NpsConfig build(Map<String, Object> raw) {
        NpsConfig config = objectMapper.convertValue(raw, NpsConfig.class);
        validateIfNeeded(config);
        return config;
    }

    @Override
    @SneakyThrows
    public NpsConfig deserialize(String json) {
        return objectMapper.readValue(json, NpsConfig.class);
    }

    @Override
    public Map<String, Object> defaultSchema() {
        return Map.of("minLabel", "", "maxLabel", "");
    }

    @Override
    public Set<ConditionOperator> supportedOperators() {
        return Set.of(ConditionOperator.EQUALS, ConditionOperator.GREATER_THAN,
                ConditionOperator.LESS_THAN, ConditionOperator.BETWEEN);
    }
}
