package com.kodelabs.formflow.modules.forms.application.service.handler;

import com.kodelabs.formflow.modules.forms.domain.model.config.QuestionConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.Validatable;

import java.util.Map;

public interface QuestionTypeHandler<T extends QuestionConfig> extends QuestionTypeHandlerSpec {

    @Override T build(Map<String, Object> raw);

    @Override T deserialize(String json);

    default void validateIfNeeded(T config) {
        if (config instanceof Validatable v) {
            v.validate();
        }
    }
}
