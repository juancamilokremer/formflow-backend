package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.UpdateTenantCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantResult;

public interface UpdateTenantUseCase {
    TenantResult execute(UpdateTenantCommand command);
}
