package com.kodelabs.formflow.modules.forms.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record QuestionType(String code) {

    @JsonCreator
    public static QuestionType of(String code) {
        return new QuestionType(code.toLowerCase());
    }

    @JsonValue
    @Override
    public String code() {
        return code;
    }
}
