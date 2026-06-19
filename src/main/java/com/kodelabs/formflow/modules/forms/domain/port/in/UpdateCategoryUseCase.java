package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateCategoryCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CategoryResult;

public interface UpdateCategoryUseCase {
    CategoryResult execute(UpdateCategoryCommand command);
}
