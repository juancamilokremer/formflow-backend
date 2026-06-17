package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.DeleteFormCommand;

public interface DeleteFormUseCase {
    void execute(DeleteFormCommand command);
}
