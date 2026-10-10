package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.RequestPlanUpgradeCommand;

public interface RequestPlanUpgradeUseCase {

    void execute(RequestPlanUpgradeCommand command);
}
