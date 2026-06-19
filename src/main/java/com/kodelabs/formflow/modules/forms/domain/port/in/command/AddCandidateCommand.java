package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.util.UUID;

public record AddCandidateCommand(
        UUID convocatoriaId,
        UUID tenantId,
        UUID userId,
        String name,
        String email
) {}
