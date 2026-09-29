package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.ActivateTenantCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantResult;

public interface ActivateTenantUseCase {
    TenantResult execute(ActivateTenantCommand command);
}
