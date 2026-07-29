package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaFormResult;

import java.util.List;
import java.util.UUID;

public record ConvocatoriaFormResponse(
        UUID id,
        UUID formId,
        int weight,
        List<CategoryWeight> categoryWeights,
        Integer minScore,
        int position
) {
    public static ConvocatoriaFormResponse from(ConvocatoriaFormResult r) {
        return new ConvocatoriaFormResponse(
                r.id(), r.formId(), r.weight(), r.categoryWeights(), r.minScore(), r.position());
    }
}
