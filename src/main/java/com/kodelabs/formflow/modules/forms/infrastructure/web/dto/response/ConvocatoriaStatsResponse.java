package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaStatsResult;

import java.util.UUID;

public record ConvocatoriaStatsResponse(
        UUID convocatoriaId,
        String convocatoriaName,
        int total,
        int responded,
        int pending,
        int aptoCount,
        int revisarCount,
        int noAptoCount,
        double participationPct
) {
    public static ConvocatoriaStatsResponse from(ConvocatoriaStatsResult r) {
        return new ConvocatoriaStatsResponse(
                r.convocatoriaId(), r.convocatoriaName(),
                r.total(), r.responded(), r.pending(),
                r.aptoCount(), r.revisarCount(), r.noAptoCount(),
                r.participationPct()
        );
    }
}
