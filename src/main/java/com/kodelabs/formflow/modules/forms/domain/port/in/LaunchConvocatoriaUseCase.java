package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.LaunchConvocatoriaCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaResult;

public interface LaunchConvocatoriaUseCase {
    ConvocatoriaResult execute(LaunchConvocatoriaCommand command);
}
