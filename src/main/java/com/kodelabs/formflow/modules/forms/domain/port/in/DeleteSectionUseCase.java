package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.DeleteSectionCommand;

public interface DeleteSectionUseCase {
    void execute(DeleteSectionCommand command);
}
