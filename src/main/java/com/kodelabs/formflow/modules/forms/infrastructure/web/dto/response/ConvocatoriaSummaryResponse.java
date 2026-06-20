package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaSummaryResult;

import java.time.Instant;
import java.util.UUID;

public record ConvocatoriaSummaryResponse(
        UUID id,
        String name,
        String status,
        long candidateCount,
        Instant startDate,
        Instant endDate,
        Instant createdAt
) {
    public static ConvocatoriaSummaryResponse from(ConvocatoriaSummaryResult r) {
        return new ConvocatoriaSummaryResponse(
                r.id(), r.name(), r.status(), r.candidateCount(),
                r.startDate(), r.endDate(), r.createdAt()
        );
    }
}
