package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request;

import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionalLogic;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

public record UpdateQuestionRequest(
        @NotBlank @Size(max = 500) String title,
        @Size(max = 2000) String description,
        @NotNull QuestionType type,
        boolean required,
        UUID categoryId,
        Integer timeLimitSeconds,
        ConditionalLogic conditionalLogic,
        Map<String, Object> config
) {}
