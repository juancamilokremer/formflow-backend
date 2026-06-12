package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.RegisterTenantCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.AuthResult;

/**
 * Input port: registers a new company (tenant) together with its admin user
 * and returns the tokens for the initial session.
 */
public interface RegisterTenantUseCase {

    AuthResult execute(RegisterTenantCommand command);
}
