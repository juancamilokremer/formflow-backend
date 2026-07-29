package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.AddConvocatoriaFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaFormResult;

public interface AddConvocatoriaFormUseCase {
    ConvocatoriaFormResult execute(AddConvocatoriaFormCommand command);
}
