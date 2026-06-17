package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.CreateFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormSummaryResult;

public interface CreateFormUseCase {
    FormSummaryResult execute(CreateFormCommand command);
}
