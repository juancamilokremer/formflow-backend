package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.DownloadAnswerFileCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.DownloadAnswerFileResult;

public interface DownloadAnswerFileUseCase {
    DownloadAnswerFileResult execute(DownloadAnswerFileCommand command);
}
