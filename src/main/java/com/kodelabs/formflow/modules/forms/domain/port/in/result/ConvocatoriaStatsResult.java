package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import java.util.UUID;

public record ConvocatoriaStatsResult(
        UUID convocatoriaId,
        String convocatoriaName,
        int total,
        int notStarted,
        int inProgress,
        int responded,
        int aptoCount,
        int revisarCount,
        int noAptoCount,
        double participationPct
) {}
