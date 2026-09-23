package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.CreateConvocatoriaFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaFormResult;

public interface CreateConvocatoriaFormUseCase {
    ConvocatoriaFormResult execute(CreateConvocatoriaFormCommand command);
}
