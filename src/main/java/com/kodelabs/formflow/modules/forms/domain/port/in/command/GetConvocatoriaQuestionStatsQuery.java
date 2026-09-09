package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.time.Instant;
import java.util.UUID;

public record GetConvocatoriaQuestionStatsQuery(
        UUID convocatoriaId, UUID tenantId, Instant submittedAtFrom, Instant submittedAtTo) {}
