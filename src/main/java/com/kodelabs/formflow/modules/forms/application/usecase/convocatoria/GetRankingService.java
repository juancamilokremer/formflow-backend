package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetRankingUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetRankingQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidateResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetRankingService implements GetRankingUseCase {

    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final CandidateRepositoryPort candidateRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CandidateResult> execute(GetRankingQuery query) {
        validateConvocatoriaExists(query);
        return candidateRepository.findAllByConvocatoriaId(query.convocatoriaId())
                .stream()
                .filter(c -> c.getScores() != null)
                .sorted(Comparator.comparingDouble((Candidate c) -> c.getScores().total()).reversed())
                .map(CandidateResult::from)
                .toList();
    }

    private void validateConvocatoriaExists(GetRankingQuery query) {
        if (!convocatoriaRepository.existsByIdAndTenantId(query.convocatoriaId(), query.tenantId())) {
            throw new BusinessException("error.convocatoria.not_found",
                    HttpStatus.NOT_FOUND, query.convocatoriaId());
        }
    }
}
