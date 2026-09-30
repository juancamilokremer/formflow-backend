package com.kodelabs.formflow.modules.auth.domain.port.in.command;

import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.model.TenantStatus;

/** status/plan may be null to skip that filter. */
public record ListTenantsQuery(int page, int size, TenantStatus status, TenantPlan plan) {}
