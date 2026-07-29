package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.port.in.UpdateConvocatoriaFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateConvocatoriaFormCommand;
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
public class UpdateConvocatoriaFormService implements UpdateConvocatoriaFormUseCase {

    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final ConvocatoriaFormRepositoryPort convocatoriaFormRepository;

    @Override
    @Transactional
    public ConvocatoriaFormResult execute(UpdateConvocatoriaFormCommand command) {
        Convocatoria convocatoria = loadDraftConvocatoria(command.convocatoriaId(), command.tenantId());
        ConvocatoriaForm form = convocatoria.getForms().stream()
                .filter(f -> f.getId().equals(command.convocatoriaFormId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "error.convocatoria.form_not_found", HttpStatus.NOT_FOUND, command.convocatoriaFormId()));

        form.setWeight(command.weight());
        form.setCategoryWeights(command.categoryWeights() != null ? command.categoryWeights() : List.of());
        form.setMinScore(command.minScore());

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
}
