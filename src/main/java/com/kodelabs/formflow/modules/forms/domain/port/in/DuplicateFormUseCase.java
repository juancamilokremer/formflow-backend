package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.DuplicateFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormSummaryResult;

public interface DuplicateFormUseCase {
    FormSummaryResult execute(DuplicateFormCommand command);
}
