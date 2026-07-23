package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.util.UUID;

public record GetFormVersionHistoryQuery(
        UUID formId,
        UUID tenantId
) {}
