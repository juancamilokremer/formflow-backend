package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateQuestionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionResult;

public interface UpdateQuestionUseCase {
    QuestionResult execute(UpdateQuestionCommand command);
}
