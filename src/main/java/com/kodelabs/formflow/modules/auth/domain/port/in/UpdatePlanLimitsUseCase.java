package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.UpdatePlanLimitsCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.PlanLimitsResult;

public interface UpdatePlanLimitsUseCase {
    PlanLimitsResult execute(UpdatePlanLimitsCommand command);
}
