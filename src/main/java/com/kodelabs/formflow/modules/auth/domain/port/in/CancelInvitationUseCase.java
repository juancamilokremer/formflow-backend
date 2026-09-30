package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.CancelInvitationCommand;

public interface CancelInvitationUseCase {
    void execute(CancelInvitationCommand command);
}
