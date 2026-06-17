package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.util.UUID;

public record UpdateSectionCommand(
        UUID sectionId,
        UUID formId,
        UUID tenantId,
        String title,
        String description
) {}
