package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;

import java.util.Map;
import java.util.UUID;

public record AddQuestionCommand(
        UUID formId,
        UUID sectionId,
        UUID tenantId,
        UUID userId,
        String title,
        String description,
        QuestionType type,
        boolean required,
        UUID categoryId,
        Integer timeLimitSeconds,
        Map<String, Object> rawConfig
) {}
