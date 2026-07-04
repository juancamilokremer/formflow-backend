package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import java.time.Instant;

public record PublicCandidateFormResult(
        String candidateName,
        String convocatoriaName,
        Instant endDate,
        boolean alreadyResponded,
        PublicFormResult form
) {}
