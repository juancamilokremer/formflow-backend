package com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Null limit fields mean "unlimited" — Jakarta's @Min does not fire on null, so a
 *  present value must still be >= 0 while leaving a field out keeps it uncapped. */
public record UpdatePlanLimitsRequest(
        @Min(value = 0, message = "{validation.plan_limits.min}")
        Integer formsLimit,
        @Min(value = 0, message = "{validation.plan_limits.min}")
        Integer responsesLimit,
        @Min(value = 0, message = "{validation.plan_limits.min}")
        Integer usersLimit,
        @Min(value = 0, message = "{validation.plan_limits.min}")
        Integer convocatoriasLimit,
        @NotNull(message = "{validation.plan_limits.can_export_excel_required}")
        Boolean canExportExcel
) {}
