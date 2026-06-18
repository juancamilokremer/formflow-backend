package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.util.UUID;

public record UpdateCategoryCommand(
        UUID id,
        UUID tenantId,
        String name,
        String color,
        String description
) {}
