package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ScoringConfig;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ConvocatoriaResponse(
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
        List<CandidateResponse> candidates
) {
    public static ConvocatoriaResponse from(ConvocatoriaResult r) {
        return new ConvocatoriaResponse(
                r.id(), r.tenantId(), r.formId(), r.name(), r.type(), r.status(),
                r.categoryWeights(), r.scoringConfig(),
                r.startDate(), r.endDate(), r.createdAt(), r.updatedAt(),
                r.candidates().stream().map(CandidateResponse::from).toList()
        );
    }
}
