package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import java.util.UUID;

public record AnswerValueResult(UUID questionId, Object value) {}
