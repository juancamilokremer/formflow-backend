package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.util.UUID;

public record GenerateFormVersionCommand(
        UUID formId,
        UUID tenantId,
        UUID userId
) {}
