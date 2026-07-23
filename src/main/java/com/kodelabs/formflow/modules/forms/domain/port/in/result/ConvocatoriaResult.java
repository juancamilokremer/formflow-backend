package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ScoringConfig;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ConvocatoriaResult(
        UUID id,
        UUID tenantId,
        UUID formId,
        String name,
        FormType type,
        String status,
        List<CategoryWeight> categoryWeights,
        ScoringConfig scoringConfig,
        Instant startDate,
        Instant endDate,
        Instant createdAt,
        Instant updatedAt,
        List<CandidateResult> candidates
) {
    public static ConvocatoriaResult from(Convocatoria c, List<Candidate> candidates) {
        return new ConvocatoriaResult(
                c.getId(), c.getTenantId(), c.getFormId(), c.getName(), c.getType(),
                c.getStatus().name(), c.getCategoryWeights(), c.getScoringConfig(),
                c.getStartDate(), c.getEndDate(), c.getCreatedAt(), c.getUpdatedAt(),
                candidates.stream().map(CandidateResult::from).toList()
        );
    }
}
