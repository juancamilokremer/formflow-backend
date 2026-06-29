package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionalLogic;
import com.kodelabs.formflow.modules.forms.domain.model.config.QuestionConfig;

import java.util.UUID;

public record PublicQuestionResult(
        UUID id,
        String title,
        String description,
        QuestionType type,
        int position,
        boolean required,
        Integer timeLimitSeconds,
        ConditionalLogic conditionalLogic,
        QuestionConfig config
) {}
