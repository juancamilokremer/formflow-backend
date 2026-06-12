package com.kodelabs.formflow.modules.auth.application.usecase;

/**
 * Input for LoginUseCase.
 */
public record LoginCommand(String tenantSlug, String email, String password) {}
