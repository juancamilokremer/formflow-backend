package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionalLogic;
import com.kodelabs.formflow.modules.forms.domain.model.config.QuestionConfig;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionResult;

import java.time.Instant;
import java.util.UUID;

public record QuestionResponse(
        UUID id,
        UUID sectionId,
        UUID formId,
        String title,
        String description,
        QuestionType type,
        int position,
        boolean required,
        UUID categoryId,
        Integer timeLimitSeconds,
        ConditionalLogic conditionalLogic,
        QuestionConfig config,
        Instant createdAt,
        Instant updatedAt
) {
    public static QuestionResponse from(QuestionResult r) {
        return new QuestionResponse(
                r.id(), r.sectionId(), r.formId(), r.title(), r.description(),
                r.type(), r.position(), r.required(), r.categoryId(),
                r.timeLimitSeconds(), r.conditionalLogic(), r.config(),
                r.createdAt(), r.updatedAt());
    }
}
