package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.SendConvocatoriaRemindersCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SendConvocatoriaRemindersResult;

public interface SendConvocatoriaRemindersUseCase {

    SendConvocatoriaRemindersResult execute(SendConvocatoriaRemindersCommand command);
}
