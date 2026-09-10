package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.export.CandidatePdfData;
import com.kodelabs.formflow.modules.forms.application.service.export.CandidatePdfRenderer;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateClassification;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetCandidateConvocatoriaResponseUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ExportCandidateResponsePdfQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetCandidateConvocatoriaResponseQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.AnswerDetailResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidateConvocatoriaResponseResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidateFormExportResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidateResponsePdfResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportCandidateResponsePdfServiceTest {

    @Mock private GetCandidateConvocatoriaResponseUseCase getCandidateConvocatoriaResponse;
    @Mock private CandidatePdfRenderer pdfRenderer;
    @InjectMocks private ExportCandidateResponsePdfService service;

    private final UUID convId = UUID.randomUUID();
    private final UUID tenantId = UUID.randomUUID();
    private final UUID candidateId = UUID.randomUUID();

    @Test
    void rendersThePdfFromTheAssembledDetailAndBuildsTheFilenameFromTheCandidateName() {
        List<AnswerDetailResult> answers = List.of(
                new AnswerDetailResult(UUID.randomUUID(), "¿Años?", "single", "opt1", "3-5 años"));
        CandidateFormExportResult form = new CandidateFormExportResult("Evaluación técnica", 85.0, null, answers);
        CandidateConvocatoriaResponseResult detail = new CandidateConvocatoriaResponseResult(
                "Maria Gomez", "maria@test.com", "Analista de RRHH", 85.0, CandidateClassification.APTO, List.of(form));

        when(getCandidateConvocatoriaResponse.execute(new GetCandidateConvocatoriaResponseQuery(convId, candidateId, tenantId)))
                .thenReturn(detail);
        when(pdfRenderer.render(any())).thenReturn(new byte[] {1, 2, 3});

        CandidateResponsePdfResult result = service.execute(
                new ExportCandidateResponsePdfQuery(convId, candidateId, tenantId));

        assertThat(result.content()).containsExactly(1, 2, 3);
        assertThat(result.filename()).startsWith("maria-gomez_").endsWith(".pdf");

        ArgumentCaptor<CandidatePdfData> captor = ArgumentCaptor.forClass(CandidatePdfData.class);
        verify(pdfRenderer).render(captor.capture());
        CandidatePdfData data = captor.getValue();
        assertThat(data.candidateName()).isEqualTo("Maria Gomez");
        assertThat(data.candidateEmail()).isEqualTo("maria@test.com");
        assertThat(data.convocatoriaName()).isEqualTo("Analista de RRHH");
        assertThat(data.totalScore()).isEqualTo(85.0);
        assertThat(data.classification()).isEqualTo("APTO");
        assertThat(data.forms()).isEqualTo(List.of(form));
    }

    @Test
    void mapsANullClassificationToNullInsteadOfFailing() {
        CandidateConvocatoriaResponseResult detail = new CandidateConvocatoriaResponseResult(
                "Pedro Sin Responder", "pedro@test.com", "Analista de RRHH", null, null, List.of());

        when(getCandidateConvocatoriaResponse.execute(new GetCandidateConvocatoriaResponseQuery(convId, candidateId, tenantId)))
                .thenReturn(detail);
        when(pdfRenderer.render(any())).thenReturn(new byte[] {9});

        service.execute(new ExportCandidateResponsePdfQuery(convId, candidateId, tenantId));

        ArgumentCaptor<CandidatePdfData> captor = ArgumentCaptor.forClass(CandidatePdfData.class);
        verify(pdfRenderer).render(captor.capture());
        assertThat(captor.getValue().classification()).isNull();
        assertThat(captor.getValue().forms()).isEmpty();
    }
}
