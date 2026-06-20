package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.util.UUID;

public record CloseConvocatoriaCommand(UUID id, UUID tenantId, UUID userId) {}
