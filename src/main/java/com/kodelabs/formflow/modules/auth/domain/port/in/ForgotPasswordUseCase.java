package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.ForgotPasswordCommand;

/**
 * Input port: starts the password reset flow. Always completes without
 * revealing whether the email exists (user enumeration protection).
 */
public interface ForgotPasswordUseCase {

    void execute(ForgotPasswordCommand command);
}
