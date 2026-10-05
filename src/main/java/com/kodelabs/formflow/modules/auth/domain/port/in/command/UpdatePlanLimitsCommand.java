package com.kodelabs.formflow.modules.auth.domain.port.in.command;

import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;

public record UpdatePlanLimitsCommand(
        TenantPlan plan,
        int formsLimit,
        int responsesLimit,
        int usersLimit,
        int convocatoriasLimit,
        boolean canExportExcel
) {}
