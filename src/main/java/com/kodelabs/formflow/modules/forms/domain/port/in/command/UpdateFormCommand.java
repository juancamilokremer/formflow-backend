package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.util.UUID;

public record UpdateFormCommand(
        UUID formId,
        UUID tenantId,
        UUID userId,
        String name,
        String description,
        Integer timeLimitSeconds
) {}
