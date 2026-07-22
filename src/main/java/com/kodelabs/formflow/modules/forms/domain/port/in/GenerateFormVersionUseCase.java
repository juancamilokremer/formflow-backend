package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.GenerateFormVersionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormSummaryResult;

public interface GenerateFormVersionUseCase {
    FormSummaryResult execute(GenerateFormVersionCommand command);
}
