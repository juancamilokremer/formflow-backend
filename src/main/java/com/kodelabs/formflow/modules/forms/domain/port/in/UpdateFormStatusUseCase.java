package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateFormStatusCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormSummaryResult;

public interface UpdateFormStatusUseCase {
    FormSummaryResult execute(UpdateFormStatusCommand command);
}
