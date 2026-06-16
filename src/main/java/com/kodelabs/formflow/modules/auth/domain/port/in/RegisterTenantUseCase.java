package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.RegisterTenantCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.RegisterTenantResult;

/**
 * Input port: registers a new company (tenant) together with its admin user.
 * Does not issue session tokens — the admin must confirm their email via the
 * verification link before being able to log in.
 */
public interface RegisterTenantUseCase {

    RegisterTenantResult execute(RegisterTenantCommand command);
}
