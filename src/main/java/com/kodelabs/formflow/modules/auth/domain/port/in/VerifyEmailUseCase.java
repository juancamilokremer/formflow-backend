package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.VerifyEmailCommand;

/**
 * Input port: confirms a user's email address with a single-use token.
 */
public interface VerifyEmailUseCase {

    void execute(VerifyEmailCommand command);
}
