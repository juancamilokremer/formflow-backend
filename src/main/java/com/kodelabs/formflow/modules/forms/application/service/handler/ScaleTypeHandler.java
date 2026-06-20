package com.kodelabs.formflow.modules.forms.application.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.ScaleConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.ScoringType;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionOperator;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ScaleTypeHandler implements QuestionTypeHandler<ScaleConfig> {

    public static final QuestionType QUESTION_TYPE = new QuestionType("SCALE");

    private final ObjectMapper objectMapper;

    @Override
    public QuestionType type() {
        return QUESTION_TYPE;
    }

    @Override
    public ScaleConfig build(Map<String, Object> raw) {
        ScaleConfig config = objectMapper.convertValue(raw, ScaleConfig.class);
        if (config.getScoringType() == ScoringType.AUTO) {
            config.calculateAutoScores();
        }
        validateIfNeeded(config);
        return config;
    }

    @Override
    @SneakyThrows
    public ScaleConfig deserialize(String json) {
        return objectMapper.readValue(json, ScaleConfig.class);
    }

    @Override
    public Map<String, Object> defaultSchema() {
        return Map.of("min", 1, "max", 5, "minLabel", "", "maxLabel", "", "scoringType", "NONE");
    }

    @Override
    public Set<ConditionOperator> supportedOperators() {
        return Set.of(ConditionOperator.EQUALS, ConditionOperator.GREATER_THAN,
                ConditionOperator.LESS_THAN, ConditionOperator.BETWEEN);
    }
}
