package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateClassification;

import java.util.List;

public record CandidateConvocatoriaResponseResult(
        String candidateName,
        String candidateEmail,
        String convocatoriaName,
        Double totalScore,
        CandidateClassification classification,
        List<CandidateFormExportResult> forms) {}
