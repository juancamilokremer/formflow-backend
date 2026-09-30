package com.kodelabs.formflow.modules.auth.domain.port.in.result;

import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;

import java.util.Map;

public record GlobalStatsResult(
        long totalTenants,
        long responsesThisMonth,
        Map<TenantPlan, Long> tenantsByPlan
) {}
