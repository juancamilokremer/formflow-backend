package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetTenantByIdQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantResult;

public interface GetTenantByIdUseCase {
    TenantResult execute(GetTenantByIdQuery query);
}
