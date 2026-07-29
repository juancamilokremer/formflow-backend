package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.RemoveConvocatoriaFormCommand;

public interface RemoveConvocatoriaFormUseCase {
    void execute(RemoveConvocatoriaFormCommand command);
}
