package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.conditional.Condition;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionalLogic;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.LogicOperator;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
public class ConditionalLogicEvaluator {

    public boolean isVisible(ConditionalLogic logic, Map<UUID, Object> answers) {
        if (logic == null) return true;
        boolean met = evaluateAll(logic.conditions(), logic.logicOperator(), answers);
        return switch (logic.action()) {
            case SHOW -> met;
            case HIDE -> !met;
        };
    }

    private boolean evaluateAll(List<Condition> conditions, LogicOperator operator, Map<UUID, Object> answers) {
        return switch (operator) {
            case AND -> conditions.stream().allMatch(c -> evaluate(c, answers));
            case OR  -> conditions.stream().anyMatch(c -> evaluate(c, answers));
        };
    }

    private boolean evaluate(Condition condition, Map<UUID, Object> answers) {
        Object answer = answers.get(condition.sourceQuestionId());
        Object value  = condition.value();

        return switch (condition.operator()) {
            case EQUALS       -> Objects.equals(stringify(answer), stringify(value));
            case NOT_EQUALS   -> !Objects.equals(stringify(answer), stringify(value));
            case CONTAINS     -> containsValue(answer, value);
            case NOT_CONTAINS -> !containsValue(answer, value);
            case GREATER_THAN -> compareNumbers(answer, value) > 0;
            case LESS_THAN    -> compareNumbers(answer, value) < 0;
            case BETWEEN      -> isBetween(answer, value);
            case IS_ANSWERED  -> answer != null && !answer.toString().isBlank();
            case IS_EMPTY     -> answer == null || answer.toString().isBlank();
            case BEFORE       -> compareStrings(answer, value) < 0;
            case AFTER        -> compareStrings(answer, value) > 0;
        };
    }

    private String stringify(Object obj) {
        return obj == null ? null : obj.toString();
    }

    private boolean containsValue(Object answer, Object value) {
        if (answer instanceof Collection<?> col) {
            String target = stringify(value);
            return col.stream().map(Object::toString).anyMatch(v -> v.equals(target));
        }
        return answer != null && answer.toString().contains(value != null ? value.toString() : "");
    }

    private double compareNumbers(Object answer, Object value) {
        if (answer == null || value == null) return 0;
        try {
            return Double.parseDouble(answer.toString()) - Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean isBetween(Object answer, Object value) {
        if (answer == null || !(value instanceof List<?> range) || range.size() < 2) return false;
        try {
            double num = Double.parseDouble(answer.toString());
            double min = Double.parseDouble(range.get(0).toString());
            double max = Double.parseDouble(range.get(1).toString());
            return num >= min && num <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private int compareStrings(Object answer, Object value) {
        if (answer == null && value == null) return 0;
        if (answer == null) return -1;
        if (value == null) return 1;
        return answer.toString().compareTo(value.toString());
    }
}
