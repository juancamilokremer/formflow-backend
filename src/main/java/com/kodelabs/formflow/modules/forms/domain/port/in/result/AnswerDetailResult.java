package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import java.util.UUID;

public record AnswerDetailResult(
        UUID questionId,
        String questionTitle,
        String questionType,
        Object value,
        String displayValue
) {}
