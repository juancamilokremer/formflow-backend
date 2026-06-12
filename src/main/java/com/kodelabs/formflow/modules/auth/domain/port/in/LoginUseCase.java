package com.kodelabs.formflow.modules.auth.domain.port.in;

/**
 * Input port: authenticates a user within its tenant
 * (email + password + company slug).
 */
public interface LoginUseCase {

    AuthResult execute(LoginCommand command);
}
