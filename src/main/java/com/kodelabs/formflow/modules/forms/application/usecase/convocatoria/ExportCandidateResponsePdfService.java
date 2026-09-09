package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.CandidateClassifier;
import com.kodelabs.formflow.modules.forms.application.service.ResponseDetailAssembler;
import com.kodelabs.formflow.modules.forms.application.service.export.CandidatePdfData;
import com.kodelabs.formflow.modules.forms.application.service.export.CandidatePdfRenderer;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateClassification;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateFormScore;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.port.in.ExportCandidateResponsePdfUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ExportCandidateResponsePdfQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidateFormExportResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidateResponsePdfResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExportCandidateResponsePdfService implements ExportCandidateResponsePdfUseCase {

    private static final DateTimeFormatter FILENAME_DATE = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);

    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final CandidateRepositoryPort candidateRepository;
    private final FormRepositoryPort formRepository;
    private final FormResponseRepositoryPort responseRepository;
    private final ResponseDetailAssembler responseDetailAssembler;
    private final CandidateClassifier candidateClassifier;
    private final CandidatePdfRenderer pdfRenderer;

    @Override
    @Transactional(readOnly = true)
    public CandidateResponsePdfResult execute(ExportCandidateResponsePdfQuery query) {
        Convocatoria convocatoria = loadConvocatoria(query.convocatoriaId(), query.tenantId());
        Candidate candidate = loadCandidate(query.candidateId(), query.convocatoriaId());
        List<FormResponse> responses = responseRepository.findAllByCandidateIdAndConvocatoriaId(
                candidate.getId(), convocatoria.getId(), query.tenantId());
        Map<UUID, String> formNames = loadFormNames(convocatoria, query.tenantId());

        List<CandidateFormExportResult> forms = responses.stream()
                .map(response -> toFormExportResult(response, candidate, formNames, query.tenantId()))
                .toList();

        CandidateClassification classification = candidateClassifier.classify(
                candidate.getScores(), convocatoria.getForms(), convocatoria.getScoringConfig());

        byte[] content = pdfRenderer.render(new CandidatePdfData(
                candidate.getName(), candidate.getEmail(), convocatoria.getName(),
                responseDetailAssembler.resolveScore(candidate),
                classification != null ? classification.name() : null,
                forms));

        return new CandidateResponsePdfResult(content, buildFilename(candidate.getName()));
    }

    private Convocatoria loadConvocatoria(UUID convocatoriaId, UUID tenantId) {
        return convocatoriaRepository.findByIdAndTenantId(convocatoriaId, tenantId)
                .orElseThrow(() -> new BusinessException(
                        "error.convocatoria.not_found", HttpStatus.NOT_FOUND, convocatoriaId));
    }

    private Candidate loadCandidate(UUID candidateId, UUID convocatoriaId) {
        return candidateRepository.findByIdAndConvocatoriaId(candidateId, convocatoriaId)
                .orElseThrow(() -> new BusinessException(
                        "error.candidate.not_found", HttpStatus.NOT_FOUND, candidateId));
    }

    private Map<UUID, String> loadFormNames(Convocatoria convocatoria, UUID tenantId) {
        Map<UUID, String> names = new HashMap<>();
        for (ConvocatoriaForm cf : convocatoria.getForms()) {
            formRepository.findByIdAndTenantId(cf.getFormId(), tenantId)
                    .ifPresent(f -> names.put(f.getId(), f.getName()));
        }
        return names;
    }

    private CandidateFormExportResult toFormExportResult(
            FormResponse response, Candidate candidate, Map<UUID, String> formNames, UUID tenantId) {
        return new CandidateFormExportResult(
                formNames.getOrDefault(response.getFormId(), "Formulario"),
                resolveFormScore(candidate, response.getFormId()),
                responseDetailAssembler.resolveCategoryScores(candidate, response.getFormId(), tenantId),
                responseDetailAssembler.buildOrderedAnswers(response));
    }

    private Double resolveFormScore(Candidate candidate, UUID formId) {
        if (candidate.getScores() == null || candidate.getScores().perForm() == null) return null;
        return candidate.getScores().perForm().stream()
                .filter(formScoreEntry -> formId.equals(formScoreEntry.formId()))
                .findFirst()
                .map(CandidateFormScore::total)
                .orElse(null);
    }

    private String buildFilename(String candidateName) {
        String slug = candidateName.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        return slug + "_" + FILENAME_DATE.format(Instant.now()) + ".pdf";
    }
}
