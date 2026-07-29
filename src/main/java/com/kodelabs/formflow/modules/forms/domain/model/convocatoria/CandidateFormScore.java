package com.kodelabs.formflow.modules.forms.domain.model.convocatoria;

import java.util.Map;
import java.util.UUID;

public record CandidateFormScore(UUID convocatoriaFormId, UUID formId, double total, Map<UUID, Double> byCategory) {}
