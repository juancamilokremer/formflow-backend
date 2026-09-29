package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.UploadAnswerFileCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.UploadAnswerFileResult;

public interface UploadAnswerFileUseCase {
    UploadAnswerFileResult execute(UploadAnswerFileCommand command);
}
