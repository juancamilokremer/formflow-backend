package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.UploadAvatarCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.MeResult;

public interface UploadAvatarUseCase {
    MeResult execute(UploadAvatarCommand command);
}
