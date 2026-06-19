package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.CreateCategoryCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CategoryResult;

public interface CreateCategoryUseCase {
    CategoryResult execute(CreateCategoryCommand command);
}
