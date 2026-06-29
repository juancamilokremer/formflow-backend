package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.SubmitPublicResponseCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SubmitPublicResponseResult;

public interface SubmitPublicResponseUseCase {
    SubmitPublicResponseResult execute(SubmitPublicResponseCommand command);
}
