package com.kodelabs.formflow.modules.auth.domain.port.in;

/**
 * Input port: single-use refresh token rotation — validates the incoming
 * token, revokes it and issues a new access + refresh pair.
 */
public interface RefreshTokenUseCase {

    AuthResult execute(RefreshTokenCommand command);
}
