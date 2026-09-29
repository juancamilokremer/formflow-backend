package com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.GlobalStatsResult;

import java.util.Map;

public record GlobalStatsResponse(
        long totalTenants,
        long responsesThisMonth,
        Map<TenantPlan, Long> tenantsByPlan
) {
    public static GlobalStatsResponse from(GlobalStatsResult r) {
        return new GlobalStatsResponse(r.totalTenants(), r.responsesThisMonth(), r.tenantsByPlan());
    }
}
