package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateClassification;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ScoringConfig;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetConvocatoriaStatsUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetConvocatoriaStatsQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaStatsResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetConvocatoriaStatsService implements GetConvocatoriaStatsUseCase {

    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final CandidateRepositoryPort candidateRepository;

    @Override
    @Transactional(readOnly = true)
    public ConvocatoriaStatsResult execute(GetConvocatoriaStatsQuery query) {
        Convocatoria convocatoria = loadConvocatoria(query);
        List<Candidate> candidates = candidateRepository.findAllByConvocatoriaId(query.convocatoriaId());
        return computeStats(convocatoria, candidates);
    }

    private Convocatoria loadConvocatoria(GetConvocatoriaStatsQuery query) {
        return convocatoriaRepository.findByIdAndTenantId(query.convocatoriaId(), query.tenantId())
                .orElseThrow(() -> new BusinessException("error.convocatoria.not_found",
                        HttpStatus.NOT_FOUND, query.convocatoriaId()));
    }

    private ConvocatoriaStatsResult computeStats(Convocatoria convocatoria, List<Candidate> candidates) {
        int total = candidates.size();
        List<Candidate> respondedList = candidates.stream()
                .filter(c -> c.getScores() != null)
                .toList();
        int responded = respondedList.size();
        int pending = total - responded;

        ScoringConfig config = convocatoria.getScoringConfig();
        int aptoCount    = (int) respondedList.stream().filter(c -> classify(c, config) == CandidateClassification.APTO).count();
        int revisarCount = (int) respondedList.stream().filter(c -> classify(c, config) == CandidateClassification.REVISAR).count();
        int noAptoCount  = (int) respondedList.stream().filter(c -> classify(c, config) == CandidateClassification.NO_APTO).count();

        double participationPct = total == 0 ? 0.0 : (responded * 100.0) / total;

        return new ConvocatoriaStatsResult(
                convocatoria.getId(), convocatoria.getName(),
                total, responded, pending,
                aptoCount, revisarCount, noAptoCount,
                Math.round(participationPct * 10.0) / 10.0
        );
    }

    private CandidateClassification classify(Candidate c, ScoringConfig config) {
        double score = c.getScores().total();
        if (score >= config.aptoMin())    return CandidateClassification.APTO;
        if (score >= config.revisarMin()) return CandidateClassification.REVISAR;
        return CandidateClassification.NO_APTO;
    }
}
