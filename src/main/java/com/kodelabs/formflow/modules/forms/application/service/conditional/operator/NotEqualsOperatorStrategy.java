package com.kodelabs.formflow.modules.forms.application.service.conditional.operator;

import com.kodelabs.formflow.modules.forms.application.service.conditional.OperatorStrategy;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionOperator;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class NotEqualsOperatorStrategy implements OperatorStrategy {

    @Override
    public ConditionOperator operator() {
        return ConditionOperator.NOT_EQUALS;
    }

    @Override
    public boolean evaluate(Object answer, Object value) {
        return !Objects.equals(ConditionalValueHelper.stringify(answer),
                ConditionalValueHelper.stringify(value));
    }
}
