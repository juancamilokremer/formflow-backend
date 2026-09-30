package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.InviteUserCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.InvitationResult;

public interface InviteUserUseCase {
    InvitationResult execute(InviteUserCommand command);
}
