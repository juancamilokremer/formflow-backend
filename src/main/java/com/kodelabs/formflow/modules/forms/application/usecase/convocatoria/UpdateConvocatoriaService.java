package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.ConvocatoriaFormValidator;
import com.kodelabs.formflow.modules.forms.application.service.ConvocatoriaWeightValidator;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.port.in.UpdateConvocatoriaUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateConvocatoriaCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaFormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateConvocatoriaService implements UpdateConvocatoriaUseCase {

    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final ConvocatoriaFormRepositoryPort convocatoriaFormRepository;
    private final CandidateRepositoryPort candidateRepository;
    private final ConvocatoriaFormValidator formValidator;
    private final ConvocatoriaWeightValidator weightValidator;

    @Override
    @Transactional
    public ConvocatoriaResult execute(UpdateConvocatoriaCommand command) {
        Convocatoria convocatoria = loadDraftConvocatoria(command.id(), command.tenantId());
        weightValidator.validate(command.categoryWeights());
        applyUpdates(convocatoria, command);
        Convocatoria saved = convocatoriaRepository.save(convocatoria);
        var candidates = candidateRepository.findAllByConvocatoriaId(saved.getId());
        return ConvocatoriaResult.from(saved, candidates);
    }

    private Convocatoria loadDraftConvocatoria(UUID id, UUID tenantId) {
        Convocatoria convocatoria = convocatoriaRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new BusinessException("error.convocatoria.not_found", HttpStatus.NOT_FOUND, id));
        if (!convocatoria.isDraft()) {
            throw new BusinessException("error.convocatoria.not_draft", HttpStatus.CONFLICT);
        }
        return convocatoria;
    }

    private void applyUpdates(Convocatoria convocatoria, UpdateConvocatoriaCommand command) {
        convocatoria.setName(command.name());
        if (command.scoringConfig() != null) convocatoria.setScoringConfig(command.scoringConfig());
        applyFormUpdates(convocatoria, command);
    }

    private void applyFormUpdates(Convocatoria convocatoria, UpdateConvocatoriaCommand command) {
        if (command.formId() == null && command.categoryWeights() == null) return;

        ConvocatoriaForm form = convocatoria.getForms().isEmpty()
                ? ConvocatoriaForm.builder()
                        .convocatoriaId(convocatoria.getId())
                        .weight(100)
                        .categoryWeights(new ArrayList<>())
                        .position(0)
                        .build()
                : convocatoria.getForms().get(0);

        if (command.formId() != null) {
            formValidator.validateExists(command.formId(), command.tenantId());
            form.setFormId(command.formId());
        }
        if (command.categoryWeights() != null) form.setCategoryWeights(command.categoryWeights());

        // A ConvocatoriaForm row always requires a form_id (DB constraint) — if there's still no form
        // attached at this point, there is nothing to persist yet (weights-only update is a no-op).
        if (form.getFormId() == null) return;

        convocatoria.setForms(List.of(convocatoriaFormRepository.save(form)));
    }
}
