package com.kodelabs.formflow.modules.auth.domain.port.in;

/**
 * Input of RefreshTokenUseCase — part of the input port contract.
 */
public record RefreshTokenCommand(String refreshToken) {}
