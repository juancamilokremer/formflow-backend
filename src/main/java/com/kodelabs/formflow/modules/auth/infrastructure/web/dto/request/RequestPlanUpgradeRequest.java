package com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request;

import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import jakarta.validation.constraints.NotNull;

public record RequestPlanUpgradeRequest(
        @NotNull(message = "{validation.plan.required}")
        TenantPlan requestedPlan,
        String message
) {}
