package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.conditional.Condition;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionalLogic;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionalLogicAction;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionOperator;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.LogicOperator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConditionalLogicEvaluatorTest {

    private final ConditionalLogicEvaluator evaluator = new ConditionalLogicEvaluator();

    private final UUID qId = UUID.randomUUID();

    @Test
    void nullLogicIsAlwaysVisible() {
        assertThat(evaluator.isVisible(null, Map.of())).isTrue();
    }

    @Test
    void showActionVisibleWhenConditionMet() {
        var logic = show(List.of(condition(ConditionOperator.EQUALS, "A")));
        assertThat(evaluator.isVisible(logic, Map.of(qId, "A"))).isTrue();
    }

    @Test
    void showActionHiddenWhenConditionNotMet() {
        var logic = show(List.of(condition(ConditionOperator.EQUALS, "A")));
        assertThat(evaluator.isVisible(logic, Map.of(qId, "B"))).isFalse();
    }

    @Test
    void hideActionHiddenWhenConditionMet() {
        var logic = hide(List.of(condition(ConditionOperator.EQUALS, "A")));
        assertThat(evaluator.isVisible(logic, Map.of(qId, "A"))).isFalse();
    }

    @Test
    void andRequiresAllConditions() {
        UUID q2 = UUID.randomUUID();
        var logic = new ConditionalLogic(ConditionalLogicAction.SHOW, LogicOperator.AND, List.of(
                new Condition(qId, ConditionOperator.EQUALS, "X"),
                new Condition(q2, ConditionOperator.EQUALS, "Y")
        ));
        assertThat(evaluator.isVisible(logic, Map.of(qId, "X", q2, "Z"))).isFalse();
        assertThat(evaluator.isVisible(logic, Map.of(qId, "X", q2, "Y"))).isTrue();
    }

    @Test
    void orRequiresAtLeastOneCondition() {
        UUID q2 = UUID.randomUUID();
        var logic = new ConditionalLogic(ConditionalLogicAction.SHOW, LogicOperator.OR, List.of(
                new Condition(qId, ConditionOperator.EQUALS, "X"),
                new Condition(q2, ConditionOperator.EQUALS, "Y")
        ));
        assertThat(evaluator.isVisible(logic, Map.of(qId, "X", q2, "Z"))).isTrue();
        assertThat(evaluator.isVisible(logic, Map.of(qId, "A", q2, "B"))).isFalse();
    }

    @Test
    void greaterThanOperator() {
        var logic = show(List.of(condition(ConditionOperator.GREATER_THAN, 3)));
        assertThat(evaluator.isVisible(logic, Map.of(qId, 5))).isTrue();
        assertThat(evaluator.isVisible(logic, Map.of(qId, 2))).isFalse();
    }

    @Test
    void betweenOperator() {
        var logic = show(List.of(new Condition(qId, ConditionOperator.BETWEEN, List.of(3, 7))));
        assertThat(evaluator.isVisible(logic, Map.of(qId, 5))).isTrue();
        assertThat(evaluator.isVisible(logic, Map.of(qId, 10))).isFalse();
    }

    @Test
    void isAnsweredOperator() {
        var logic = show(List.of(condition(ConditionOperator.IS_ANSWERED, null)));
        assertThat(evaluator.isVisible(logic, Map.of(qId, "text"))).isTrue();
        assertThat(evaluator.isVisible(logic, Map.of())).isFalse();
    }

    @Test
    void containsOperatorOnList() {
        var logic = show(List.of(condition(ConditionOperator.CONTAINS, "opt1")));
        assertThat(evaluator.isVisible(logic, Map.of(qId, List.of("opt1", "opt2")))).isTrue();
        assertThat(evaluator.isVisible(logic, Map.of(qId, List.of("opt3")))).isFalse();
    }

    private ConditionalLogic show(List<Condition> conditions) {
        return new ConditionalLogic(ConditionalLogicAction.SHOW, LogicOperator.AND, conditions);
    }

    private ConditionalLogic hide(List<Condition> conditions) {
        return new ConditionalLogic(ConditionalLogicAction.HIDE, LogicOperator.AND, conditions);
    }

    private Condition condition(ConditionOperator op, Object value) {
        return new Condition(qId, op, value);
    }
}
