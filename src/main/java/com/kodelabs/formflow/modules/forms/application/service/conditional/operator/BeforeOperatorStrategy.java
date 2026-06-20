package com.kodelabs.formflow.modules.forms.application.service.conditional.operator;

import com.kodelabs.formflow.modules.forms.application.service.conditional.OperatorStrategy;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionOperator;
import org.springframework.stereotype.Component;

@Component
public class BeforeOperatorStrategy implements OperatorStrategy {

    @Override
    public ConditionOperator operator() {
        return ConditionOperator.BEFORE;
    }

    // ISO 8601 dates (YYYY-MM-DD) compare correctly as strings lexicographically
    @Override
    public boolean evaluate(Object answer, Object value) {
        if (answer == null || value == null) return false;
        return answer.toString().compareTo(value.toString()) < 0;
    }
}
