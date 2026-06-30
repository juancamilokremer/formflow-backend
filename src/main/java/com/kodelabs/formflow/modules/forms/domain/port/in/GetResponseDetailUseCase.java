package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetResponseDetailQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ResponseDetailResult;

public interface GetResponseDetailUseCase {
    ResponseDetailResult execute(GetResponseDetailQuery query);
}
