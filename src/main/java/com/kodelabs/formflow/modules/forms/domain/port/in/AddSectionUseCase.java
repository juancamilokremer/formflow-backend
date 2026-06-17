package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.AddSectionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SectionResult;

public interface AddSectionUseCase {
    SectionResult execute(AddSectionCommand command);
}
