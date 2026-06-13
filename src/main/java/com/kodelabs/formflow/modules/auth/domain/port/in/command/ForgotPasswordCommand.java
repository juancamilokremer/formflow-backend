package com.kodelabs.formflow.modules.auth.domain.port.in.command;

/**
 * Input of ForgotPasswordUseCase — part of the input port contract.
 */
public record ForgotPasswordCommand(String tenantSlug, String email) {}
