package com.kodelabs.formflow.modules.auth.domain.port.in;

/**
 * Input of LoginUseCase — part of the input port contract.
 */
public record LoginCommand(String tenantSlug, String email, String password) {}
