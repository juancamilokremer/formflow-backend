package com.kodelabs.formflow.modules.auth.domain.port.in.result;

import com.kodelabs.formflow.modules.auth.domain.model.PlanLimits;
import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;

public record PlanLimitsResult(
        TenantPlan plan,
        int formsLimit,
        int responsesLimit,
        int usersLimit,
        int convocatoriasLimit,
        boolean canExportExcel
) {
    public static PlanLimitsResult of(TenantPlan plan, PlanLimits limits) {
        return new PlanLimitsResult(
                plan, limits.formsLimit(), limits.responsesLimit(), limits.usersLimit(),
                limits.convocatoriasLimit(), limits.canExportExcel());
    }
}
