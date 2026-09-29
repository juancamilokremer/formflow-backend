package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.ChangeTenantPlanCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantResult;

public interface ChangeTenantPlanUseCase {
    TenantResult execute(ChangeTenantPlanCommand command);
}
