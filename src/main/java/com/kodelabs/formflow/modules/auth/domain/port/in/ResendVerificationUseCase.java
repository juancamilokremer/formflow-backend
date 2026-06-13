package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.ResendVerificationCommand;

/**
 * Input port: re-sends the verification email to the authenticated user.
 */
public interface ResendVerificationUseCase {

    void execute(ResendVerificationCommand command);
}
