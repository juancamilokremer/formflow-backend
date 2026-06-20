package com.kodelabs.formflow.modules.forms.application.service.handler;

import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionOperator;
import com.kodelabs.formflow.modules.forms.domain.model.config.QuestionConfig;

import java.util.Map;
import java.util.Set;

public interface QuestionTypeHandlerSpec {

    QuestionType type();

    QuestionConfig build(Map<String, Object> raw);

    QuestionConfig deserialize(String json);

    Map<String, Object> defaultSchema();

    Set<ConditionOperator> supportedOperators();
}
