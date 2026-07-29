package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateConvocatoriaFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaFormResult;

public interface UpdateConvocatoriaFormUseCase {
    ConvocatoriaFormResult execute(UpdateConvocatoriaFormCommand command);
}
