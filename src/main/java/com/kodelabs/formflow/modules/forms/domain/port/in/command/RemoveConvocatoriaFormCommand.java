package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.util.UUID;

public record RemoveConvocatoriaFormCommand(UUID convocatoriaFormId, UUID convocatoriaId, UUID tenantId, UUID userId) {}
