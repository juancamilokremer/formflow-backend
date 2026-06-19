package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.port.in.ListConvocatoriasUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ListConvocatoriasQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaSummaryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListConvocatoriasService implements ListConvocatoriasUseCase {

    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final CandidateRepositoryPort candidateRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ConvocatoriaSummaryResult> execute(ListConvocatoriasQuery query) {
        return convocatoriaRepository.findActiveByTenantId(query.tenantId())
                .stream()
                .map(c -> ConvocatoriaSummaryResult.from(c,
                        candidateRepository.countByConvocatoriaId(c.getId())))
                .toList();
    }
}
