package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateClassification;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidateConvocatoriaResponseResult;

import java.util.List;

public record CandidateConvocatoriaResponseResponse(
        String candidateName,
        String candidateEmail,
        String convocatoriaName,
        Double totalScore,
        CandidateClassification classification,
        List<CandidateFormExportResponse> forms
) {
    public static CandidateConvocatoriaResponseResponse from(CandidateConvocatoriaResponseResult r) {
        return new CandidateConvocatoriaResponseResponse(
                r.candidateName(), r.candidateEmail(), r.convocatoriaName(), r.totalScore(),
                r.classification(),
                r.forms().stream().map(CandidateFormExportResponse::from).toList());
    }
}
