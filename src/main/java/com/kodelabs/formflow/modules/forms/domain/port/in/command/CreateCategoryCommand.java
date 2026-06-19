package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.util.UUID;

public record CreateCategoryCommand(
        UUID tenantId,
        String name,
        String color,
        String description
) {}
