package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request;

import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Either {@code name} (a blank form, {@code type} defaults to the convocatoria's) or
 * {@code duplicateFromId} (a copy of an existing form), never both — there is no way to
 * attach a pre-existing form, because a form always belongs to the convocatoria it was born in.
 */
public record CreateConvocatoriaFormRequest(
        @Size(max = 200) String name,
        FormType type,
        UUID duplicateFromId,
        @Min(0) @Max(100) int weight,
        @Valid List<CategoryWeightRequest> categoryWeights,
        @Min(0) @Max(100) Integer minScore
) {

    @AssertTrue(message = "{validation.convocatoria_form.source_required}")
    public boolean isExactlyOneSourceGiven() {
        boolean hasName = name != null && !name.isBlank();
        return hasName ^ (duplicateFromId != null);
    }
}
