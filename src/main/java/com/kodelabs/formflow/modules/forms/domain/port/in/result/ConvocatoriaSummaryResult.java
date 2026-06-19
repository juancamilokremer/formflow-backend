package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;

import java.time.Instant;
import java.util.UUID;

public record ConvocatoriaSummaryResult(
        UUID id,
        String name,
        String status,
        long candidateCount,
        Instant startDate,
        Instant endDate,
        Instant createdAt
) {
    public static ConvocatoriaSummaryResult from(Convocatoria c, long candidateCount) {
        return new ConvocatoriaSummaryResult(
                c.getId(), c.getName(), c.getStatus().name(),
                candidateCount, c.getStartDate(), c.getEndDate(), c.getCreatedAt()
        );
    }
}
