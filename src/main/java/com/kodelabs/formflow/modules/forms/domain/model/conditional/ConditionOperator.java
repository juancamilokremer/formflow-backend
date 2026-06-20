package com.kodelabs.formflow.modules.forms.domain.model.conditional;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ConditionOperator {
    EQUALS, NOT_EQUALS,
    CONTAINS, NOT_CONTAINS,
    GREATER_THAN, LESS_THAN, BETWEEN,
    IS_ANSWERED, IS_EMPTY,
    BEFORE, AFTER;

    @JsonCreator
    public static ConditionOperator of(String value) {
        return valueOf(value.toUpperCase());
    }
}
