package com.kodelabs.formflow.modules.forms.domain.model.conditional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;

import java.util.Map;
import java.util.Set;

public enum ConditionOperator {
    EQUALS, NOT_EQUALS,
    CONTAINS, NOT_CONTAINS,
    GREATER_THAN, LESS_THAN, BETWEEN,
    IS_ANSWERED, IS_EMPTY,
    BEFORE, AFTER;

    private static final Map<String, Set<ConditionOperator>> VALID_BY_TYPE = Map.of(
            "SINGLE",   Set.of(EQUALS, NOT_EQUALS),
            "MULTIPLE", Set.of(CONTAINS, NOT_CONTAINS),
            "SCALE",    Set.of(EQUALS, GREATER_THAN, LESS_THAN, BETWEEN),
            "NPS",      Set.of(EQUALS, GREATER_THAN, LESS_THAN, BETWEEN),
            "TEXT",     Set.of(IS_ANSWERED, IS_EMPTY, CONTAINS),
            "DATE",     Set.of(EQUALS, BEFORE, AFTER)
    );

    public static boolean isValidFor(ConditionOperator operator, QuestionType type) {
        Set<ConditionOperator> valid = VALID_BY_TYPE.get(type.code());
        return valid != null && valid.contains(operator);
    }

    @JsonCreator
    public static ConditionOperator of(String value) {
        return valueOf(value.toUpperCase());
    }
}
