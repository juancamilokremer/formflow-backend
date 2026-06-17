package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetFormQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormDetailResult;

public interface GetFormUseCase {
    FormDetailResult execute(GetFormQuery query);
}
