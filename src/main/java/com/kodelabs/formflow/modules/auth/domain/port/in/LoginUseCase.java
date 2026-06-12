package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.LoginCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.AuthResult;

/**
 * Input port: authenticates a user within its tenant
 * (email + password + company slug).
 */
public interface LoginUseCase {

    AuthResult execute(LoginCommand command);
}
