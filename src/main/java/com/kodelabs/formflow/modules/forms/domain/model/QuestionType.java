package com.kodelabs.formflow.modules.forms.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum QuestionType {

    TEXT("text"),
    SINGLE("single"),
    MULTIPLE("multiple"),
    SCALE("scale"),
    NPS("nps"),
    MATRIX("matrix"),
    DATE("date"),
    FILE("file"),
    INFO("info");

    private final String code;

    QuestionType(String code) {
        this.code = code;
    }

    @JsonValue
    public String code() {
        return code;
    }

    @JsonCreator
    public static QuestionType of(String code) {
        if (code == null) return null;
        String normalized = code.toLowerCase();
        for (QuestionType type : values()) {
            if (type.code.equals(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown QuestionType: " + code);
    }
}
