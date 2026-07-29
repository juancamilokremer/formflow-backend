package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.ConvocatoriaFormValidator;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.port.in.AddConvocatoriaFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.AddConvocatoriaFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaFormResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaFormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddConvocatoriaFormService implements AddConvocatoriaFormUseCase {

    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final ConvocatoriaFormRepositoryPort convocatoriaFormRepository;
    private final ConvocatoriaFormValidator formValidator;

    @Override
    @Transactional
    public ConvocatoriaFormResult execute(AddConvocatoriaFormCommand command) {
        Convocatoria convocatoria = loadDraftConvocatoria(command.convocatoriaId(), command.tenantId());
        formValidator.validateExists(command.formId(), command.tenantId());
        validateNotAlreadyAttached(convocatoria, command.formId());

        int nextPosition = convocatoriaFormRepository.countByConvocatoriaId(convocatoria.getId());

        ConvocatoriaForm form = ConvocatoriaForm.builder()
                .convocatoriaId(convocatoria.getId())
                .formId(command.formId())
                .weight(command.weight())
                .categoryWeights(command.categoryWeights() != null ? command.categoryWeights() : List.of())
                .minScore(command.minScore())
                .position(nextPosition)
                .build();

        return ConvocatoriaFormResult.from(convocatoriaFormRepository.save(form));
    }

    private Convocatoria loadDraftConvocatoria(UUID id, UUID tenantId) {
        Convocatoria convocatoria = convocatoriaRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new BusinessException("error.convocatoria.not_found", HttpStatus.NOT_FOUND, id));
        if (!convocatoria.isDraft()) {
            throw new BusinessException("error.convocatoria.not_draft", HttpStatus.CONFLICT);
        }
        return convocatoria;
    }

    private void validateNotAlreadyAttached(Convocatoria convocatoria, UUID formId) {
        boolean alreadyAttached = convocatoria.getForms().stream().anyMatch(f -> f.getFormId().equals(formId));
        if (alreadyAttached) {
            throw new BusinessException("error.convocatoria.form_already_attached", HttpStatus.CONFLICT, formId);
        }
    }
}
