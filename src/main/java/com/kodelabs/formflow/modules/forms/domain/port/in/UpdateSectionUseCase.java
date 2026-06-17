package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateSectionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SectionResult;

public interface UpdateSectionUseCase {
    SectionResult execute(UpdateSectionCommand command);
}
