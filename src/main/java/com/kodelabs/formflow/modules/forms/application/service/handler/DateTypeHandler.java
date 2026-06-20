package com.kodelabs.formflow.modules.forms.application.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.DateConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DateTypeHandler implements QuestionTypeHandler<DateConfig> {

    public static final QuestionType QUESTION_TYPE = new QuestionType("DATE");

    private final ObjectMapper objectMapper;

    @Override
    public QuestionType type() {
        return QUESTION_TYPE;
    }

    @Override
    public DateConfig build(Map<String, Object> raw) {
        DateConfig config = objectMapper.convertValue(raw, DateConfig.class);
        validateIfNeeded(config);
        return config;
    }

    @Override
    @SneakyThrows
    public DateConfig deserialize(String json) {
        return objectMapper.readValue(json, DateConfig.class);
    }

    @Override
    public Map<String, Object> defaultSchema() {
        return Map.of("includeTime", false);
    }
}
