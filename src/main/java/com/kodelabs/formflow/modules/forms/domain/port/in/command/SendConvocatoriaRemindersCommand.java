package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.util.UUID;

public record SendConvocatoriaRemindersCommand(UUID convocatoriaId, UUID tenantId) {}
