package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.ListCategoriesQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CategoryResult;

import java.util.List;

public interface ListCategoriesUseCase {
    List<CategoryResult> execute(ListCategoriesQuery query);
}
