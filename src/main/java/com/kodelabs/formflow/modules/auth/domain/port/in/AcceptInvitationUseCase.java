package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.AcceptInvitationCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.AcceptInvitationResult;

public interface AcceptInvitationUseCase {
    AcceptInvitationResult execute(AcceptInvitationCommand command);
}
