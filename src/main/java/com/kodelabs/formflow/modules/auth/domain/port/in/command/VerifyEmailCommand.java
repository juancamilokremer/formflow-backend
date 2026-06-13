package com.kodelabs.formflow.modules.auth.domain.port.in.command;

/**
 * Input of VerifyEmailUseCase — part of the input port contract.
 */
public record VerifyEmailCommand(String token) {}
