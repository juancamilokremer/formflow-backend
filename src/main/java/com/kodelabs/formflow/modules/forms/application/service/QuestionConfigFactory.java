package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.QuestionConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class QuestionConfigFactory {

    private final QuestionTypeRegistry registry;

    public QuestionConfig build(QuestionType type, Map<String, Object> raw) {
        return registry.get(type).build(raw == null ? Map.of() : raw);
    }
}
