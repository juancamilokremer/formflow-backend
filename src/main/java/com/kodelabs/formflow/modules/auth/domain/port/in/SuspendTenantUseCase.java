package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.SuspendTenantCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantResult;

public interface SuspendTenantUseCase {
    TenantResult execute(SuspendTenantCommand command);
}
