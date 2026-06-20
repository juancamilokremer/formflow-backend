package com.kodelabs.formflow.modules.forms.application.service.conditional.operator;

import com.kodelabs.formflow.modules.forms.application.service.conditional.OperatorStrategy;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionOperator;
import org.springframework.stereotype.Component;

@Component
public class IsAnsweredOperatorStrategy implements OperatorStrategy {

    @Override
    public ConditionOperator operator() {
        return ConditionOperator.IS_ANSWERED;
    }

    @Override
    public boolean evaluate(Object answer, Object value) {
        return answer != null && !answer.toString().isBlank();
    }
}
