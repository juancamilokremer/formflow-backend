package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;

import java.util.List;
import java.util.UUID;

/**
 * Creates a form already attached to its convocatoria, in a single transaction.
 * Exactly one source must be given: {@code name} + {@code type} for a blank form, or
 * {@code duplicateFromId} to copy an existing one. A form never exists unattached.
 */
public record CreateConvocatoriaFormCommand(
        UUID convocatoriaId,
        UUID tenantId,
        UUID userId,
        String name,
        FormType type,
        UUID duplicateFromId,
        int weight,
        List<CategoryWeight> categoryWeights,
        Integer minScore
) {
    public boolean isDuplicate() {
        return duplicateFromId != null;
    }
}
