package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetPublicFormQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.PublicFormResult;

public interface GetPublicFormUseCase {
    PublicFormResult execute(GetPublicFormQuery query);
}
