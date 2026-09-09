package com.kodelabs.formflow.modules.forms.application.service.export;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidateFormExportResult;

import java.util.List;

/** Input for CandidatePdfRenderer — a flat view model, decoupled from the domain result shape. */
public record CandidatePdfData(
        String candidateName,
        String candidateEmail,
        String convocatoriaName,
        Double totalScore,
        String classification,
        List<CandidateFormExportResult> forms) {}
