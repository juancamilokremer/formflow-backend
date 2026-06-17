package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.util.UUID;

public record AddSectionCommand(
        UUID formId,
        UUID tenantId,
        UUID userId,
        String title,
        String description
) {}
