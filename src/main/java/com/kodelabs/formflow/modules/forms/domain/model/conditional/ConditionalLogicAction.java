package com.kodelabs.formflow.modules.forms.domain.model.conditional;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ConditionalLogicAction {
    SHOW, HIDE;

    @JsonCreator
    public static ConditionalLogicAction of(String value) {
        return valueOf(value.toUpperCase());
    }
}
