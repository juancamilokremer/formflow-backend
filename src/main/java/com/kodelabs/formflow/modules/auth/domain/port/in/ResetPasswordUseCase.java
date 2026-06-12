package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.ResetPasswordCommand;

/**
 * Input port: consumes a reset token, updates the password and revokes
 * every active refresh token of the user.
 */
public interface ResetPasswordUseCase {

    void execute(ResetPasswordCommand command);
}
