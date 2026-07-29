package com.kodelabs.formflow.modules.forms.application.usecase.form;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetCandidatePortalUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetCandidatePortalQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidatePortalFormResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidatePortalResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class GetCandidatePortalService implements GetCandidatePortalUseCase {

    private final CandidateRepositoryPort candidateRepository;
    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final FormRepositoryPort formRepository;
    private final FormResponseRepositoryPort responseRepository;

    @Override
    public CandidatePortalResult execute(GetCandidatePortalQuery query) {
        // Public endpoint — no TenantContext available. The token itself is the identity proof.
        Candidate candidate = candidateRepository.findByToken(query.candidateToken())
                .orElseThrow(() -> new BusinessException(
                        "error.candidate.token_not_found", HttpStatus.NOT_FOUND));

        Convocatoria convocatoria = convocatoriaRepository
                .findByIdAndTenantId(candidate.getConvocatoriaId(), candidate.getTenantId())
                .orElseThrow(() -> new BusinessException(
                        "error.convocatoria.not_found", HttpStatus.NOT_FOUND));

        if (convocatoria.getStatus() == ConvocatoriaStatus.CLOSED) {
            throw new BusinessException("error.convocatoria.closed", HttpStatus.CONFLICT);
        }

        var forms = convocatoria.getForms().stream()
                .sorted(Comparator.comparingInt(ConvocatoriaForm::getPosition))
                .map(cf -> toPortalForm(cf, candidate))
                .toList();

        return new CandidatePortalResult(
                candidate.getName(),
                convocatoria.getName(),
                convocatoria.getEndDate(),
                candidate.getStatus() == CandidateStatus.RESPONDED,
                forms);
    }

    private CandidatePortalFormResult toPortalForm(ConvocatoriaForm convocatoriaForm, Candidate candidate) {
        Form form = formRepository.findByIdAndTenantId(convocatoriaForm.getFormId(), candidate.getTenantId())
                .orElseThrow(() -> new BusinessException(
                        "error.form.not_found", HttpStatus.NOT_FOUND, convocatoriaForm.getFormId()));
        boolean completed = responseRepository.existsByCandidateIdAndFormId(
                candidate.getId(), convocatoriaForm.getFormId());
        return new CandidatePortalFormResult(form.getId(), form.getName(), completed);
    }
}
