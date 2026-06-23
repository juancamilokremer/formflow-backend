package com.kodelabs.formflow.modules.forms.domain.model.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ScoringType {
    AUTO, MANUAL, NONE;

    @JsonCreator
    public static ScoringType fromString(String value) {
        return ScoringType.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }
}
