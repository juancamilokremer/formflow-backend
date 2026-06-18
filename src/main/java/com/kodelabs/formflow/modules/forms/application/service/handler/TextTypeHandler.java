package com.kodelabs.formflow.modules.forms.application.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.TextConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TextTypeHandler implements QuestionTypeHandler<TextConfig> {

    public static final QuestionType TYPE = new QuestionType("TEXT");

    private final ObjectMapper objectMapper;

    @Override
    public QuestionType type() {
        return TYPE;
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
}
