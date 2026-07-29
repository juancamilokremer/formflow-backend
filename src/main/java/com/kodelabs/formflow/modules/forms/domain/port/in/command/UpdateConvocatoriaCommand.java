package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ScoringConfig;

import java.util.UUID;

public record UpdateConvocatoriaCommand(
        UUID id,
        UUID tenantId,
        UUID userId,
        String name,
        ScoringConfig scoringConfig
) {}
