package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateConvocatoriaCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaResult;

public interface UpdateConvocatoriaUseCase {
    ConvocatoriaResult execute(UpdateConvocatoriaCommand command);
}
