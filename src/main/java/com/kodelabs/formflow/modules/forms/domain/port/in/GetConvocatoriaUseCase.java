package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetConvocatoriaQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaResult;

public interface GetConvocatoriaUseCase {
    ConvocatoriaResult execute(GetConvocatoriaQuery query);
}
