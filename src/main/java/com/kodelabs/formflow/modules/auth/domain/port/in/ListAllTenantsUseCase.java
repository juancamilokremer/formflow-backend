package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.ListTenantsQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantPageResult;

public interface ListAllTenantsUseCase {
    TenantPageResult execute(ListTenantsQuery query);
}
