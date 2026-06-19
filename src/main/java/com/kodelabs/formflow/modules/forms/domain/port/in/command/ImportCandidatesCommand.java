package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.util.UUID;

public record ImportCandidatesCommand(
        UUID convocatoriaId,
        UUID tenantId,
        UUID userId,
        byte[] csvContent
) {}
