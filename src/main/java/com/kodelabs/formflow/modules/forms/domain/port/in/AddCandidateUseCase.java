package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.AddCandidateCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidateResult;

public interface AddCandidateUseCase {
    CandidateResult execute(AddCandidateCommand command);
}
