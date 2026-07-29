package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.port.in.RemoveConvocatoriaFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.RemoveConvocatoriaFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaFormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RemoveConvocatoriaFormService implements RemoveConvocatoriaFormUseCase {

    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final ConvocatoriaFormRepositoryPort convocatoriaFormRepository;

    @Override
    @Transactional
    public void execute(RemoveConvocatoriaFormCommand command) {
        Convocatoria convocatoria = loadDraftConvocatoria(command.convocatoriaId(), command.tenantId());
        boolean exists = convocatoria.getForms().stream()
                .anyMatch(f -> f.getId().equals(command.convocatoriaFormId()));
        if (!exists) {
            throw new BusinessException(
                    "error.convocatoria.form_not_found", HttpStatus.NOT_FOUND, command.convocatoriaFormId());
        }
        convocatoriaFormRepository.deleteById(command.convocatoriaFormId());
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
