package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;

import java.util.List;
import java.util.UUID;

public record ConvocatoriaFormResult(
        UUID id,
        UUID formId,
        int weight,
        List<CategoryWeight> categoryWeights,
        Integer minScore,
        int position
) {
    public static ConvocatoriaFormResult from(ConvocatoriaForm f) {
        return new ConvocatoriaFormResult(
                f.getId(), f.getFormId(), f.getWeight(), f.getCategoryWeights(), f.getMinScore(), f.getPosition());
    }
}
