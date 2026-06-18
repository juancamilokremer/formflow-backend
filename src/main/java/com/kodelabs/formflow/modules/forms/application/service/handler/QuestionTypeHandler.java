package com.kodelabs.formflow.modules.forms.application.service.handler;

import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.QuestionConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.Validatable;

import java.util.Map;

public interface QuestionTypeHandler<T extends QuestionConfig> {

    QuestionType type();

    T build(Map<String, Object> raw);

    T deserialize(String json);

    Map<String, Object> defaultSchema();

    default void validateIfNeeded(T config) {
        if (config instanceof Validatable v) {
            v.validate();
        }
    }
}
