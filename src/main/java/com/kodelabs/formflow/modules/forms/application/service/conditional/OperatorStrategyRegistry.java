package com.kodelabs.formflow.modules.forms.application.service.conditional;

import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionOperator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OperatorStrategyRegistry {

    private final Map<ConditionOperator, OperatorStrategy> strategies;

    public OperatorStrategyRegistry(List<OperatorStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(OperatorStrategy::operator, s -> s));
    }

    public OperatorStrategy get(ConditionOperator operator) {
        OperatorStrategy strategy = strategies.get(operator);
        if (strategy == null) {
            throw new IllegalStateException("No strategy registered for operator: " + operator);
        }
        return strategy;
    }
}
