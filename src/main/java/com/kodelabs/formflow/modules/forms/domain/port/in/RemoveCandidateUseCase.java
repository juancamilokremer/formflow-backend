package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.RemoveCandidateCommand;

public interface RemoveCandidateUseCase {
    void execute(RemoveCandidateCommand command);
}
