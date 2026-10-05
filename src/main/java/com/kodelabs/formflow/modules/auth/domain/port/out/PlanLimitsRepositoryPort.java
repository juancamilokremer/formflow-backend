package com.kodelabs.formflow.modules.auth.domain.port.out;

import com.kodelabs.formflow.modules.auth.domain.model.PlanLimits;
import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;

import java.util.Map;

public interface PlanLimitsRepositoryPort {

    PlanLimits findByPlan(TenantPlan plan);

    Map<TenantPlan, PlanLimits> findAll();

    void save(TenantPlan plan, PlanLimits limits);
}
