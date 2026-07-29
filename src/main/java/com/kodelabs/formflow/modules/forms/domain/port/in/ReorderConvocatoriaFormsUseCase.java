package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.ReorderConvocatoriaFormsCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaFormResult;

import java.util.List;

public interface ReorderConvocatoriaFormsUseCase {
    List<ConvocatoriaFormResult> execute(ReorderConvocatoriaFormsCommand command);
}
