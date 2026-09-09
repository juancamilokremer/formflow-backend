package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.CandidateClassifier;
import com.kodelabs.formflow.modules.forms.application.service.ResponseDetailAssembler;
import com.kodelabs.formflow.modules.forms.application.service.export.CandidatePdfData;
import com.kodelabs.formflow.modules.forms.application.service.export.CandidatePdfRenderer;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateFormScore;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateScores;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ScoringConfig;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ExportCandidateResponsePdfQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.AnswerDetailResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidateResponsePdfResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportCandidateResponsePdfServiceTest {

    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private CandidateRepositoryPort candidateRepository;
    @Mock private FormRepositoryPort formRepository;
    @Mock private FormResponseRepositoryPort responseRepository;
    @Mock private ResponseDetailAssembler responseDetailAssembler;
    @Spy private CandidateClassifier candidateClassifier = new CandidateClassifier();
    @Mock private CandidatePdfRenderer pdfRenderer;
    @InjectMocks private ExportCandidateResponsePdfService service;

    private final UUID convId = UUID.randomUUID();
    private final UUID tenantId = UUID.randomUUID();
    private final UUID candidateId = UUID.randomUUID();
    private final UUID convocatoriaFormId = UUID.randomUUID();
    private final UUID formId = UUID.randomUUID();

    @Test
    void assemblesOneFormBlockPerRespondedFormAndRendersPdf() {
        Convocatoria convocatoria = convocatoriaWithOneForm();
        Candidate candidate = candidateWithScore();
        Form form = Form.builder().id(formId).tenantId(tenantId).name("Evaluación técnica").type(FormType.CANDIDATES).build();
        FormResponse response = FormResponse.builder().id(UUID.randomUUID()).formId(formId).build();
        List<AnswerDetailResult> answers = List.of(
                new AnswerDetailResult(UUID.randomUUID(), "¿Años?", "single", "opt1", "3-5 años"));

        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(convocatoria));
        when(candidateRepository.findByIdAndConvocatoriaId(candidateId, convId)).thenReturn(Optional.of(candidate));
        when(responseRepository.findAllByCandidateIdAndConvocatoriaId(candidateId, convId, tenantId))
                .thenReturn(List.of(response));
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.of(form));
        when(responseDetailAssembler.resolveScore(candidate)).thenReturn(85.0);
        when(responseDetailAssembler.resolveCategoryScores(candidate, formId, tenantId)).thenReturn(null);
        when(responseDetailAssembler.buildOrderedAnswers(response)).thenReturn(answers);
        when(pdfRenderer.render(any())).thenReturn(new byte[] {1, 2, 3});

        CandidateResponsePdfResult result = service.execute(
                new ExportCandidateResponsePdfQuery(convId, candidateId, tenantId));

        assertThat(result.content()).containsExactly(1, 2, 3);
        assertThat(result.filename()).startsWith("maria-gomez_").endsWith(".pdf");

        ArgumentCaptor<CandidatePdfData> captor = ArgumentCaptor.forClass(CandidatePdfData.class);
        org.mockito.Mockito.verify(pdfRenderer).render(captor.capture());
        CandidatePdfData data = captor.getValue();
        assertThat(data.candidateName()).isEqualTo("Maria Gomez");
        assertThat(data.totalScore()).isEqualTo(85.0);
        assertThat(data.classification()).isEqualTo("APTO");
        assertThat(data.forms()).hasSize(1);
        assertThat(data.forms().get(0).formName()).isEqualTo("Evaluación técnica");
        assertThat(data.forms().get(0).answers()).isEqualTo(answers);
    }

    @Test
    void candidateWithNoResponsesYet_rendersEmptyFormsListWithoutFailing() {
        Convocatoria convocatoria = convocatoriaWithOneForm();
        Candidate candidate = Candidate.builder()
                .id(candidateId).convocatoriaId(convId).tenantId(tenantId)
                .name("Pedro Sin Responder").email("pedro@test.com")
                .build();

        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(convocatoria));
        when(candidateRepository.findByIdAndConvocatoriaId(candidateId, convId)).thenReturn(Optional.of(candidate));
        when(responseRepository.findAllByCandidateIdAndConvocatoriaId(candidateId, convId, tenantId))
                .thenReturn(List.of());
        when(responseDetailAssembler.resolveScore(candidate)).thenReturn(null);
        when(pdfRenderer.render(any())).thenReturn(new byte[] {9});

        service.execute(new ExportCandidateResponsePdfQuery(convId, candidateId, tenantId));

        ArgumentCaptor<CandidatePdfData> captor = ArgumentCaptor.forClass(CandidatePdfData.class);
        org.mockito.Mockito.verify(pdfRenderer).render(captor.capture());
        assertThat(captor.getValue().forms()).isEmpty();
        assertThat(captor.getValue().classification()).isNull();
    }

    @Test
    void throwsNotFoundWhenConvocatoriaDoesNotBelongToTenant() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new ExportCandidateResponsePdfQuery(convId, candidateId, tenantId)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void throwsNotFoundWhenCandidateDoesNotBelongToConvocatoria() {
        Convocatoria convocatoria = convocatoriaWithOneForm();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(convocatoria));
        when(candidateRepository.findByIdAndConvocatoriaId(candidateId, convId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new ExportCandidateResponsePdfQuery(convId, candidateId, tenantId)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private Convocatoria convocatoriaWithOneForm() {
        return Convocatoria.builder()
                .id(convId).tenantId(tenantId).name("Analista de RRHH")
                .status(ConvocatoriaStatus.ACTIVE)
                .forms(List.of(ConvocatoriaForm.builder().id(convocatoriaFormId).formId(formId).weight(100).build()))
                .scoringConfig(new ScoringConfig(70, 50))
                .build();
    }

    private Candidate candidateWithScore() {
        CandidateFormScore formScore = new CandidateFormScore(convocatoriaFormId, formId, 85.0, Map.of());
        return Candidate.builder()
                .id(candidateId).convocatoriaId(convId).tenantId(tenantId)
                .name("Maria Gomez").email("maria@test.com")
                .scores(new CandidateScores(85.0, List.of(formScore)))
                .build();
    }
}
