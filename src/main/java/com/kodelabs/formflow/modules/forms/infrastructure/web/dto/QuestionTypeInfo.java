package com.kodelabs.formflow.modules.forms.infrastructure.web.dto;

import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;

import java.util.Map;

public record QuestionTypeInfo(
        QuestionType type,
        Map<String, Object> configSchema
) {}
