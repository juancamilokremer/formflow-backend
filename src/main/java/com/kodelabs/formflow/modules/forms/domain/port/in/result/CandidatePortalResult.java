package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import java.time.Instant;
import java.util.List;

public record CandidatePortalResult(
        String candidateName,
        String convocatoriaName,
        Instant endDate,
        boolean allCompleted,
        List<CandidatePortalFormResult> forms
) {}
