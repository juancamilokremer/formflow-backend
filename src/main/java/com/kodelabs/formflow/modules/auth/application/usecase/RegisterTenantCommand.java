package com.kodelabs.formflow.modules.auth.application.usecase;

/**
 * Input for RegisterTenantUseCase.
 */
public record RegisterTenantCommand(
        String companyName,
        String slug,
        String email,
        String password,
        String firstName,
        String lastName
) {}
