package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.ExportCandidateResponsePdfQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidateResponsePdfResult;

public interface ExportCandidateResponsePdfUseCase {
    CandidateResponsePdfResult execute(ExportCandidateResponsePdfQuery query);
}
