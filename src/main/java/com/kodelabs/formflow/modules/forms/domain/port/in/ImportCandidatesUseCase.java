package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.ImportCandidatesCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ImportResult;

public interface ImportCandidatesUseCase {
    ImportResult execute(ImportCandidatesCommand command);
}
