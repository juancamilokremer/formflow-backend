package com.kodelabs.formflow.modules.auth.domain.port.in.result;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;

/**
 * Result of RegisterTenantUseCase: the created admin user and tenant.
 * No tokens are issued here — the admin must confirm their email before
 * being able to log in and start a session.
 */
public record RegisterTenantResult(User user, Tenant tenant) {}
