package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.export.CandidatePdfData;
import com.kodelabs.formflow.modules.forms.application.service.export.CandidatePdfRenderer;
import com.kodelabs.formflow.modules.forms.domain.port.in.ExportCandidateResponsePdfUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetCandidateConvocatoriaResponseUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ExportCandidateResponsePdfQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetCandidateConvocatoriaResponseQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidateConvocatoriaResponseResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidateResponsePdfResult;
import com.kodelabs.formflow.shared.export.ExportFilenames;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExportCandidateResponsePdfService implements ExportCandidateResponsePdfUseCase {

    private final GetCandidateConvocatoriaResponseUseCase getCandidateConvocatoriaResponse;
    private final CandidatePdfRenderer pdfRenderer;

    @Override
    @Transactional(readOnly = true)
    public CandidateResponsePdfResult execute(ExportCandidateResponsePdfQuery query) {
        CandidateConvocatoriaResponseResult detail = getCandidateConvocatoriaResponse.execute(
                new GetCandidateConvocatoriaResponseQuery(query.convocatoriaId(), query.candidateId(), query.tenantId()));

        byte[] content = pdfRenderer.render(new CandidatePdfData(
                detail.candidateName(), detail.candidateEmail(), detail.convocatoriaName(),
                detail.totalScore(),
                detail.classification() != null ? detail.classification().name() : null,
                detail.forms()));

        return new CandidateResponsePdfResult(content, ExportFilenames.build(detail.candidateName(), "pdf"));
    }
}
