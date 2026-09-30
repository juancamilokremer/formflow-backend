package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.ChangeUserRoleCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.UserResult;

public interface ChangeUserRoleUseCase {
    UserResult execute(ChangeUserRoleCommand command);
}
