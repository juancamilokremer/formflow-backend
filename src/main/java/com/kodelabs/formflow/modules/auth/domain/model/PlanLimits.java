package com.kodelabs.formflow.modules.auth.domain.model;

/**
 * Usage limits for a subscription plan. -1 means unlimited.
 * A pure value object — values are admin-editable, stored in the plan_limits
 * table and looked up via PlanLimitsRepositoryPort (see shared.planlimit.PlanLimitsCatalog
 * for the cached lookup used by enforcement).
 */
public record PlanLimits(
        int formsLimit,
        int responsesLimit,
        int usersLimit,
        int convocatoriasLimit,
        boolean canExportExcel
) {
    public static final int UNLIMITED = -1;
}
