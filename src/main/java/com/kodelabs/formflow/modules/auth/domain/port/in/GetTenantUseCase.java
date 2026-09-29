package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetTenantQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantResult;

public interface GetTenantUseCase {
    TenantResult execute(GetTenantQuery query);
}
