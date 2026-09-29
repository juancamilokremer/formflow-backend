package com.kodelabs.formflow.modules.auth.domain.port.in.command;

import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;

import java.util.UUID;

public record ChangeTenantPlanCommand(UUID tenantId, TenantPlan plan) {}
