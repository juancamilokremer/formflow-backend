package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormSummaryResult;

public interface UpdateFormUseCase {
    FormSummaryResult execute(UpdateFormCommand command);
}
