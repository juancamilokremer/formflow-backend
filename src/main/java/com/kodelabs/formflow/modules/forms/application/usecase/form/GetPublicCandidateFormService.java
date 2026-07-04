package com.kodelabs.formflow.modules.forms.application.usecase.form;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetPublicCandidateFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetPublicFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetPublicCandidateFormQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetPublicFormQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.PublicCandidateFormResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.PublicFormResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPublicCandidateFormService implements GetPublicCandidateFormUseCase {

    private final CandidateRepositoryPort candidateRepository;
    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final GetPublicFormUseCase getPublicForm;

    @Override
    public PublicCandidateFormResult execute(GetPublicCandidateFormQuery query) {
        // Public endpoint — no TenantContext available. The token itself is the identity proof.
        Candidate candidate = candidateRepository.findByToken(query.candidateToken())
                .orElseThrow(() -> new BusinessException(
                        "error.candidate.token_not_found", HttpStatus.NOT_FOUND));

        // Derive tenant from candidate to avoid needing X-Tenant-ID header on this unauthenticated endpoint.
        Convocatoria convocatoria = convocatoriaRepository
                .findByIdAndTenantId(candidate.getConvocatoriaId(), candidate.getTenantId())
                .orElseThrow(() -> new BusinessException(
                        "error.convocatoria.not_found", HttpStatus.NOT_FOUND));

        // CLOSED = hard block; no one can submit anymore. DRAFT is never reached because
        // candidates are only invited after the convocatoria is launched (ACTIVE).
        if (convocatoria.getStatus() == ConvocatoriaStatus.CLOSED) {
            throw new BusinessException("error.convocatoria.closed", HttpStatus.CONFLICT);
        }

        PublicFormResult form = getPublicForm.execute(new GetPublicFormQuery(convocatoria.getFormId()));

        // alreadyResponded=true returns HTTP 200 (not 409) so the frontend can show a
        // personalized "ya respondiste" screen instead of a generic error page.
        return new PublicCandidateFormResult(
                candidate.getName(),
                convocatoria.getName(),
                convocatoria.getEndDate(),
                candidate.getStatus() == CandidateStatus.RESPONDED,
                form);
    }
}
