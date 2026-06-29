package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionalLogic;
import com.kodelabs.formflow.modules.forms.domain.model.config.QuestionConfig;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.PublicQuestionResult;

import java.util.UUID;

public record PublicQuestionResponse(
        UUID id,
        String title,
        String description,
        String type,
        int position,
        boolean required,
        Integer timeLimitSeconds,
        ConditionalLogic conditionalLogic,
        QuestionConfig config
) {
    public static PublicQuestionResponse from(PublicQuestionResult r) {
        return new PublicQuestionResponse(
                r.id(), r.title(), r.description(),
                r.type() != null ? r.type().code() : null,
                r.position(), r.required(), r.timeLimitSeconds(),
                r.conditionalLogic(), r.config());
    }
}
