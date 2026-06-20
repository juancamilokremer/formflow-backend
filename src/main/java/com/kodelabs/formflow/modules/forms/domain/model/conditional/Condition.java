package com.kodelabs.formflow.modules.forms.domain.model.conditional;

import java.util.UUID;

public record Condition(
        UUID sourceQuestionId,
        ConditionOperator operator,
        Object value
) {}
