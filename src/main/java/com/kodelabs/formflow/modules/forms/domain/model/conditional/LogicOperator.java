package com.kodelabs.formflow.modules.forms.domain.model.conditional;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum LogicOperator {
    AND, OR;

    @JsonCreator
    public static LogicOperator of(String value) {
        return valueOf(value.toUpperCase());
    }
}
