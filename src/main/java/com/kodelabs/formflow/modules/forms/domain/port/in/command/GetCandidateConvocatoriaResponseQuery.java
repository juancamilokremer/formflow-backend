package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.util.UUID;

public record GetCandidateConvocatoriaResponseQuery(UUID convocatoriaId, UUID candidateId, UUID tenantId) {}
