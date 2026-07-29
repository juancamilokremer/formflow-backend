package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.util.List;
import java.util.UUID;

public record ReorderConvocatoriaFormsCommand(
        UUID convocatoriaId,
        UUID tenantId,
        UUID userId,
        List<UUID> orderedConvocatoriaFormIds
) {}
