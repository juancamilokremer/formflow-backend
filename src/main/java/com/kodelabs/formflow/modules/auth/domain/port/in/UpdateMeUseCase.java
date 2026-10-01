package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.UpdateMeCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.MeResult;

public interface UpdateMeUseCase {
    MeResult execute(UpdateMeCommand command);
}
