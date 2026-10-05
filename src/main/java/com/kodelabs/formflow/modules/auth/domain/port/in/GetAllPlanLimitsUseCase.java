package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.result.PlanLimitsResult;

import java.util.List;

public interface GetAllPlanLimitsUseCase {
    List<PlanLimitsResult> execute();
}
