package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.AnswerDetailResult;

import java.util.UUID;

public record AnswerDetailResponse(
        UUID questionId,
        String questionTitle,
        String questionType,
        Object value,
        String displayValue
) {
    public static AnswerDetailResponse from(AnswerDetailResult r) {
        return new AnswerDetailResponse(r.questionId(), r.questionTitle(), r.questionType(), r.value(), r.displayValue());
    }
}
