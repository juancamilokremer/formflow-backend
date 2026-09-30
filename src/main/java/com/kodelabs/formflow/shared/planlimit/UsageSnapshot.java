package com.kodelabs.formflow.shared.planlimit;

import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;

/** Cached usage counts for one tenant — one fetch covers every PlanLimitService check. */
record UsageSnapshot(
        TenantPlan plan,
        long formsUsed,
        long responsesThisMonth,
        long usersCount,
        long convocatoriasUsed
) {}
