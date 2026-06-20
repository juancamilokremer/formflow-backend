package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.port.in.DeleteConvocatoriaUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.DeleteConvocatoriaCommand;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteConvocatoriaService implements DeleteConvocatoriaUseCase {

    private final ConvocatoriaRepositoryPort convocatoriaRepository;

    @Override
    @Transactional
    public void execute(DeleteConvocatoriaCommand command) {
        Convocatoria convocatoria = loadDraftConvocatoria(command);
        convocatoriaRepository.softDeleteById(convocatoria.getId());
    }

    private Convocatoria loadDraftConvocatoria(DeleteConvocatoriaCommand command) {
        Convocatoria convocatoria = convocatoriaRepository
                .findByIdAndTenantId(command.id(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.convocatoria.not_found",
                        HttpStatus.NOT_FOUND, command.id()));
        if (!convocatoria.isDraft()) {
            throw new BusinessException("error.convocatoria.not_draft", HttpStatus.CONFLICT);
        }
        return convocatoria;
    }
}
