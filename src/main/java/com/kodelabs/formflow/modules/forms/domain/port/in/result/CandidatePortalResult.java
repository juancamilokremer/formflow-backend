package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import com.kodelabs.formflow.modules.forms.domain.model.FormType;

import java.time.Instant;
import java.util.List;

public record CandidatePortalResult(
        String candidateName,
        String convocatoriaName,
        FormType convocatoriaType,
        Instant endDate,
        boolean allCompleted,
        List<CandidatePortalFormResult> forms
) {}
