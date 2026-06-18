package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.ReorderQuestionsCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionResult;

import java.util.List;

public interface ReorderQuestionsUseCase {
    List<QuestionResult> execute(ReorderQuestionsCommand command);
}
