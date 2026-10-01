package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.DeleteAvatarCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.MeResult;

public interface DeleteAvatarUseCase {
    MeResult execute(DeleteAvatarCommand command);
}
