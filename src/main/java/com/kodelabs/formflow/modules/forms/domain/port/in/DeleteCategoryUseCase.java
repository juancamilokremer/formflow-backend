package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.DeleteCategoryCommand;

public interface DeleteCategoryUseCase {
    void execute(DeleteCategoryCommand command);
}
