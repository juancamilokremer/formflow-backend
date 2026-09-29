package com.kodelabs.formflow.modules.auth.domain.port.in.result;

import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;

public record TenantUsageResult(
        TenantPlan plan,
        long formsUsed,
        int formsLimit,
        long responsesThisMonth,
        int responsesLimit,
        long usersCount,
        int usersLimit
) {}
