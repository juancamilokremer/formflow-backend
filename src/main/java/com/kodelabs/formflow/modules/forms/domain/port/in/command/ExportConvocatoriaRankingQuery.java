package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.util.List;
import java.util.UUID;

/** candidateIds null or empty means "export everyone". */
public record ExportConvocatoriaRankingQuery(UUID convocatoriaId, UUID tenantId, List<UUID> candidateIds) {}
