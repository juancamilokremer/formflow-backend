package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionalLogic;
import com.kodelabs.formflow.modules.forms.domain.model.config.QuestionConfig;

import java.time.Instant;
import java.util.UUID;

public record QuestionResult(
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
    public static QuestionResult from(FormQuestion q) {
        return new QuestionResult(
                q.getId(), q.getSectionId(), q.getFormId(),
                q.getTitle(), q.getDescription(), q.getType(),
                q.getPosition(), q.isRequired(), q.getCategoryId(),
                q.getTimeLimitSeconds(), q.getConditionalLogic(), q.getConfig(),
                q.getCreatedAt(), q.getUpdatedAt());
    }
}
