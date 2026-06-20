package com.kodelabs.formflow.modules.forms.domain.model.conditional;

import java.util.List;

public record ConditionalLogic(
        ConditionalLogicAction action,
        LogicOperator logicOperator,
        List<Condition> conditions
) {}
