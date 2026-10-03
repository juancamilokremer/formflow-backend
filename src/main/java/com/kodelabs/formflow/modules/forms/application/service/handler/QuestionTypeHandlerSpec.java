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

    /** Strips scoring data (option/column point values) before a config reaches an
     *  unauthenticated respondent — seeing which answer is worth more would let them
     *  game the evaluation instead of answering honestly. No-op for types with no
     *  scoring (text, date, file, info, nps); overridden by single/multiple/scale/matrix. */
    default QuestionConfig redactForPublic(QuestionConfig config) {
        return config;
    }
}
