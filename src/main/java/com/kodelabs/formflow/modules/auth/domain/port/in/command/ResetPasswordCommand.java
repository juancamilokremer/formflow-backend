package com.kodelabs.formflow.modules.auth.domain.port.in.command;

/**
 * Input of ResetPasswordUseCase — part of the input port contract.
 */
public record ResetPasswordCommand(String token, String newPassword) {}
