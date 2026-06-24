package com.kodelabs.formflow.modules.forms.application.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.InfoConfig;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionOperator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class InfoTypeHandler implements QuestionTypeHandler<InfoConfig> {

    public static final QuestionType QUESTION_TYPE = new QuestionType("info");

    private final ObjectMapper objectMapper;

    @Override
    public QuestionType type() {
        return QUESTION_TYPE;
    }

    @Override
    public InfoConfig build(Map<String, Object> raw) {
        return objectMapper.convertValue(raw, InfoConfig.class);
    }

    @Override
    @SneakyThrows
    public InfoConfig deserialize(String json) {
        return objectMapper.readValue(json, InfoConfig.class);
    }

    @Override
    public Map<String, Object> defaultSchema() {
        return Map.of("content", "");
    }

    @Override
    public Set<ConditionOperator> supportedOperators() {
        return Set.of();
    }
}
