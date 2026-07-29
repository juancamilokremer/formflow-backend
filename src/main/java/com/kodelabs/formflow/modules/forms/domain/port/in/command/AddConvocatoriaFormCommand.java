package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;

import java.util.List;
import java.util.UUID;

public record AddConvocatoriaFormCommand(
        UUID convocatoriaId,
        UUID tenantId,
        UUID userId,
        UUID formId,
        int weight,
        List<CategoryWeight> categoryWeights,
        Integer minScore
) {}
