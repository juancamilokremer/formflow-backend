package com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.PlanLimitsResult;

/** Null limit fields mean "unlimited" — friendlier for the frontend than a -1 sentinel,
 *  same convention as TenantUsageResponse. */
public record PlanLimitsResponse(
        TenantPlan plan,
        Integer formsLimit,
        Integer responsesLimit,
        Integer usersLimit,
        Integer convocatoriasLimit,
        boolean canExportExcel
) {
    public static PlanLimitsResponse from(PlanLimitsResult r) {
        return new PlanLimitsResponse(
                r.plan(), limitOrNull(r.formsLimit()), limitOrNull(r.responsesLimit()),
                limitOrNull(r.usersLimit()), limitOrNull(r.convocatoriasLimit()), r.canExportExcel());
    }

    private static Integer limitOrNull(int limit) {
        return limit < 0 ? null : limit;
    }
}
