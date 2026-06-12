package com.kodelabs.formflow.modules.auth.domain.port.in.command;

/**
 * Input of RegisterTenantUseCase — part of the input port contract.
 */
public record RegisterTenantCommand(
        String companyName,
        String slug,
        String email,
        String password,
        String firstName,
        String lastName
) {}
