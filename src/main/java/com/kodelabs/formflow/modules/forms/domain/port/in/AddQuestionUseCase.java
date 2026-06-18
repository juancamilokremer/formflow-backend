package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.AddQuestionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionResult;

public interface AddQuestionUseCase {
    QuestionResult execute(AddQuestionCommand command);
}
