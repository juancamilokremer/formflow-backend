package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record SubmitResponseRequest(
        Instant startedAt,
        @NotNull @Valid List<AnswerRequest> answers
) {}
