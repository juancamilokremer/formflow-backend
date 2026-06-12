package com.kodelabs.formflow.modules.auth.application.usecase;

/**
 * Input for RefreshTokenUseCase.
 */
public record RefreshTokenCommand(String refreshToken) {}
