package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.port.in.CloseConvocatoriaUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.CloseConvocatoriaCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CloseConvocatoriaService implements CloseConvocatoriaUseCase {

    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final CandidateRepositoryPort candidateRepository;

    @Override
    @Transactional
    public ConvocatoriaResult execute(CloseConvocatoriaCommand command) {
        Convocatoria convocatoria = loadActiveConvocatoria(command);
        convocatoria.close();
        Convocatoria saved = convocatoriaRepository.save(convocatoria);
        var candidates = candidateRepository.findAllByConvocatoriaId(saved.getId());
        return ConvocatoriaResult.from(saved, candidates);
    }

    private Convocatoria loadActiveConvocatoria(CloseConvocatoriaCommand command) {
        Convocatoria convocatoria = convocatoriaRepository
                .findByIdAndTenantId(command.id(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.convocatoria.not_found",
                        HttpStatus.NOT_FOUND, command.id()));
        if (!convocatoria.isActive()) {
            throw new BusinessException("error.convocatoria.not_active", HttpStatus.CONFLICT);
        }
        return convocatoria;
    }
}
