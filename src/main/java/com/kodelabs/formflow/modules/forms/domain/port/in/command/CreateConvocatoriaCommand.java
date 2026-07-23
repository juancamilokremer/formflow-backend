package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ScoringConfig;

import java.util.List;
import java.util.UUID;

public record CreateConvocatoriaCommand(
        UUID tenantId,
        UUID userId,
        UUID formId,
        String name,
        FormType type,
        List<CategoryWeight> categoryWeights,
        ScoringConfig scoringConfig
) {}
