package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetResponsesQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ResponsePageResult;

public interface GetResponsesUseCase {
    ResponsePageResult execute(GetResponsesQuery query);
}
