package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.CandidateClassifier;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateClassification;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
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
    private final CandidateClassifier candidateClassifier;

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
        int notStarted = (int) candidates.stream()
                .filter(c -> c.getStatus() == CandidateStatus.INVITED || c.getStatus() == CandidateStatus.EXPIRED)
                .count();
        int inProgress = (int) candidates.stream()
                .filter(c -> c.getStatus() == CandidateStatus.IN_PROGRESS)
                .count();
        List<Candidate> respondedList = candidates.stream()
                .filter(c -> c.getStatus() == CandidateStatus.RESPONDED)
                .toList();
        int responded = respondedList.size();

        int aptoCount    = (int) respondedList.stream().filter(c -> classify(c, convocatoria) == CandidateClassification.APTO).count();
        int revisarCount = (int) respondedList.stream().filter(c -> classify(c, convocatoria) == CandidateClassification.REVISAR).count();
        int noAptoCount  = (int) respondedList.stream().filter(c -> classify(c, convocatoria) == CandidateClassification.NO_APTO).count();

        double participationPct = total == 0 ? 0.0 : (responded * 100.0) / total;

        return new ConvocatoriaStatsResult(
                convocatoria.getId(), convocatoria.getName(),
                total, notStarted, inProgress, responded,
                aptoCount, revisarCount, noAptoCount,
                Math.round(participationPct * 10.0) / 10.0
        );
    }

    private CandidateClassification classify(Candidate c, Convocatoria convocatoria) {
        return candidateClassifier.classify(c.getScores(), convocatoria.getForms(), convocatoria.getScoringConfig());
    }
}
