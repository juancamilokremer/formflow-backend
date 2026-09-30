package com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantUsageResult;

/** null limit fields mean "unlimited" — friendlier for the frontend than a -1 sentinel. */
public record TenantUsageResponse(
        TenantPlan plan,
        long formsUsed,
        Integer formsLimit,
        long responsesThisMonth,
        Integer responsesLimit,
        long usersCount,
        Integer usersLimit,
        boolean canExportExcel
) {
    public static TenantUsageResponse from(TenantUsageResult r) {
        return new TenantUsageResponse(
                r.plan(), r.formsUsed(), limitOrNull(r.formsLimit()),
                r.responsesThisMonth(), limitOrNull(r.responsesLimit()),
                r.usersCount(), limitOrNull(r.usersLimit()), r.canExportExcel());
    }

    private static Integer limitOrNull(int limit) {
        return limit < 0 ? null : limit;
    }
}
