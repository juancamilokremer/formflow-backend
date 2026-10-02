package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.ChangeMyPasswordCommand;

public interface ChangeMyPasswordUseCase {
    void execute(ChangeMyPasswordCommand command);
}
