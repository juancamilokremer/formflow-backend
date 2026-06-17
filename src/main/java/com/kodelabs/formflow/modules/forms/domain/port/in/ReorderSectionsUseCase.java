package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.ReorderSectionsCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SectionResult;

import java.util.List;

public interface ReorderSectionsUseCase {
    List<SectionResult> execute(ReorderSectionsCommand command);
}
