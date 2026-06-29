package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.SubmitCandidateResponseCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SubmitCandidateResponseResult;

public interface SubmitCandidateResponseUseCase {
    SubmitCandidateResponseResult execute(SubmitCandidateResponseCommand command);
}
