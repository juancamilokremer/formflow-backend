package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AnswerRequest(
        @NotNull UUID questionId,
        Object value
) {}
