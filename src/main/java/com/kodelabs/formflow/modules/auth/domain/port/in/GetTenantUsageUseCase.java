package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetTenantUsageQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantUsageResult;

public interface GetTenantUsageUseCase {
    TenantUsageResult execute(GetTenantUsageQuery query);
}
