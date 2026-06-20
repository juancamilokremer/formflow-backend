package com.kodelabs.formflow.modules.forms.application.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.FileConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FileTypeHandler implements QuestionTypeHandler<FileConfig> {

    public static final QuestionType QUESTION_TYPE = new QuestionType("FILE");

    private final ObjectMapper objectMapper;

    @Override
    public QuestionType type() {
        return QUESTION_TYPE;
    }

    @Override
    public FileConfig build(Map<String, Object> raw) {
        FileConfig config = objectMapper.convertValue(raw, FileConfig.class);
        validateIfNeeded(config);
        return config;
    }

    @Override
    @SneakyThrows
    public FileConfig deserialize(String json) {
        return objectMapper.readValue(json, FileConfig.class);
    }

    @Override
    public Map<String, Object> defaultSchema() {
        return Map.of("maxSizeMb", 5, "allowedTypes", List.of("pdf", "jpg", "png"));
    }
}
