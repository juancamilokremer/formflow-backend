package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.CloseConvocatoriaCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaResult;

public interface CloseConvocatoriaUseCase {
    ConvocatoriaResult execute(CloseConvocatoriaCommand command);
}
