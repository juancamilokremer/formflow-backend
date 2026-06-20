package com.kodelabs.formflow.modules.forms.application.service.conditional;

import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionOperator;

public interface OperatorStrategy {

    ConditionOperator operator();

    boolean evaluate(Object answer, Object value);
}
