package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import com.kodelabs.formflow.modules.forms.domain.model.FormType;

import java.util.UUID;

public record CreateFormCommand(
        UUID tenantId,
        UUID userId,
        String name,
        String description,
        FormType type,
        Integer timeLimitSeconds
) {}
