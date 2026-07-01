package com.kodelabs.formflow.modules.forms.domain.model.convocatoria;

import java.util.UUID;

public record CandidateResponseSubmittedEvent(
        UUID candidateId,
        UUID convocatoriaId,
        UUID tenantId
) {}
