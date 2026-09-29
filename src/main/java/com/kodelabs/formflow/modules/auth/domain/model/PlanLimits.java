package com.kodelabs.formflow.modules.auth.domain.model;

/**
 * Usage limits for a subscription plan (backend#6). -1 means unlimited.
 * A static lookup only — enforcement (PlanLimitService) is out of scope for #5.
 */
public record PlanLimits(
        int formsLimit,
        int responsesLimit,
        int usersLimit,
        int convocatoriasLimit,
        boolean canExportExcel
) {

    private static final int UNLIMITED = -1;

    public static PlanLimits forPlan(TenantPlan plan) {
        return switch (plan) {
            case FREE -> new PlanLimits(2, 50, 1, 0, false);
            case STARTER -> new PlanLimits(10, 500, 3, 5, true);
            case PRO -> new PlanLimits(UNLIMITED, UNLIMITED, 10, UNLIMITED, true);
            case BUSINESS, ENTERPRISE -> new PlanLimits(UNLIMITED, UNLIMITED, UNLIMITED, UNLIMITED, true);
        };
    }
}
