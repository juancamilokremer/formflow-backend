package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.AnswerValueResult;

import java.util.UUID;

public record AnswerValueResponse(UUID questionId, Object value) {
    public static AnswerValueResponse from(AnswerValueResult r) {
        return new AnswerValueResponse(r.questionId(), r.value());
    }
}
