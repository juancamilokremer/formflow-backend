package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.application.service.conditional.OperatorStrategyRegistry;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.Condition;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionalLogic;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionalLogicAction;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.LogicOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConditionalLogicEvaluator {

    private final OperatorStrategyRegistry strategyRegistry;

    public boolean isVisible(ConditionalLogic logic, Map<UUID, Object> answers) {
        if (logic == null) return true;
        boolean met = evaluateAll(logic.conditions(), logic.logicOperator(), answers);
        ConditionalLogicAction action = logic.action() != null ? logic.action() : ConditionalLogicAction.SHOW;
        return switch (action) {
            case SHOW -> met;
            case HIDE -> !met;
        };
    }

    private boolean evaluateAll(List<Condition> conditions, LogicOperator operator,
                                Map<UUID, Object> answers) {
        return switch (operator) {
            case AND -> conditions.stream().allMatch(c -> evaluate(c, answers));
            case OR  -> conditions.stream().anyMatch(c -> evaluate(c, answers));
        };
    }

    private boolean evaluate(Condition condition, Map<UUID, Object> answers) {
        return strategyRegistry.get(condition.operator())
                .evaluate(answers.get(condition.sourceQuestionId()), condition.value());
    }
}
