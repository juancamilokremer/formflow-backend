package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.CreateConvocatoriaCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaResult;

public interface CreateConvocatoriaUseCase {
    ConvocatoriaResult execute(CreateConvocatoriaCommand command);
}
