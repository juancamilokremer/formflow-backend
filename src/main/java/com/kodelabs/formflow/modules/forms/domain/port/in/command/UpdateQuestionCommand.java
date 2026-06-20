package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionalLogic;

import java.util.Map;
import java.util.UUID;

public record UpdateQuestionCommand(
        UUID questionId,
        UUID sectionId,
        UUID formId,
        UUID tenantId,
        UUID userId,
        String title,
        String description,
        QuestionType type,
        boolean required,
        UUID categoryId,
        Integer timeLimitSeconds,
        ConditionalLogic conditionalLogic,
        Map<String, Object> rawConfig
) {}
