package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.RevokeUserAccessCommand;

public interface RevokeUserAccessUseCase {
    void execute(RevokeUserAccessCommand command);
}
