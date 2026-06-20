package com.kodelabs.formflow.modules.forms.application.service.conditional.operator;

import com.kodelabs.formflow.modules.forms.application.service.conditional.OperatorStrategy;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionOperator;
import org.springframework.stereotype.Component;

@Component
public class LessThanOperatorStrategy implements OperatorStrategy {

    @Override
    public ConditionOperator operator() {
        return ConditionOperator.LESS_THAN;
    }

    @Override
    public boolean evaluate(Object answer, Object value) {
        double a = ConditionalValueHelper.toDouble(answer);
        double v = ConditionalValueHelper.toDouble(value);
        return !Double.isNaN(a) && !Double.isNaN(v) && a < v;
    }
}
