package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.DeleteConvocatoriaCommand;

public interface DeleteConvocatoriaUseCase {
    void execute(DeleteConvocatoriaCommand command);
}
