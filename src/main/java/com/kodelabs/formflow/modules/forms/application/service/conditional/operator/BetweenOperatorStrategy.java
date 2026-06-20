package com.kodelabs.formflow.modules.forms.application.service.conditional.operator;

import com.kodelabs.formflow.modules.forms.application.service.conditional.OperatorStrategy;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionOperator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BetweenOperatorStrategy implements OperatorStrategy {

    @Override
    public ConditionOperator operator() {
        return ConditionOperator.BETWEEN;
    }

    @Override
    public boolean evaluate(Object answer, Object value) {
        if (!(value instanceof List<?> range) || range.size() < 2) return false;
        double a   = ConditionalValueHelper.toDouble(answer);
        double min = ConditionalValueHelper.toDouble(range.get(0));
        double max = ConditionalValueHelper.toDouble(range.get(1));
        return !Double.isNaN(a) && !Double.isNaN(min) && !Double.isNaN(max) && a >= min && a <= max;
    }
}
