package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;

import java.util.List;
import java.util.UUID;

public record UpdateConvocatoriaFormCommand(
        UUID convocatoriaFormId,
        UUID convocatoriaId,
        UUID tenantId,
        UUID userId,
        int weight,
        List<CategoryWeight> categoryWeights,
        Integer minScore
) {}
