package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.DeleteQuestionCommand;

public interface DeleteQuestionUseCase {
    void execute(DeleteQuestionCommand command);
}
