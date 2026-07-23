package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ScoringConfig;

import java.util.List;
import java.util.UUID;

public record UpdateConvocatoriaCommand(
        UUID id,
        UUID tenantId,
        UUID userId,
        String name,
        UUID formId,
        List<CategoryWeight> categoryWeights,
        ScoringConfig scoringConfig
) {}
