package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetConvocatoriaUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetConvocatoriaQuery;
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
public class GetConvocatoriaService implements GetConvocatoriaUseCase {

    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final CandidateRepositoryPort candidateRepository;

    @Override
    @Transactional(readOnly = true)
    public ConvocatoriaResult execute(GetConvocatoriaQuery query) {
        Convocatoria convocatoria = loadConvocatoria(query);
        var candidates = candidateRepository.findAllByConvocatoriaId(query.id());
        return ConvocatoriaResult.from(convocatoria, candidates);
    }

    private Convocatoria loadConvocatoria(GetConvocatoriaQuery query) {
        return convocatoriaRepository.findByIdAndTenantId(query.id(), query.tenantId())
                .orElseThrow(() -> new BusinessException("error.convocatoria.not_found",
                        HttpStatus.NOT_FOUND, query.id()));
    }
}
