package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetMeQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.MeResult;

public interface GetMeUseCase {
    MeResult execute(GetMeQuery query);
}
