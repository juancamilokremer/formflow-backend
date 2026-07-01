package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.Category;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateClassification;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ScoringConfig;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetRankingUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetRankingQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.RankingEntryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetRankingService implements GetRankingUseCase {

    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final CandidateRepositoryPort candidateRepository;
    private final CategoryRepositoryPort categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RankingEntryResult> execute(GetRankingQuery query) {
        Convocatoria convocatoria = loadConvocatoria(query);
        List<Candidate> candidates = candidateRepository.findAllByConvocatoriaId(query.convocatoriaId());
        Map<UUID, String> categoryNames = loadCategoryNames(convocatoria, query.tenantId());
        return buildRanking(candidates, convocatoria.getScoringConfig(), categoryNames);
    }

    private Convocatoria loadConvocatoria(GetRankingQuery query) {
        return convocatoriaRepository.findByIdAndTenantId(query.convocatoriaId(), query.tenantId())
                .orElseThrow(() -> new BusinessException("error.convocatoria.not_found",
                        HttpStatus.NOT_FOUND, query.convocatoriaId()));
    }

    private Map<UUID, String> loadCategoryNames(Convocatoria convocatoria, UUID tenantId) {
        List<UUID> categoryIds = convocatoria.getCategoryWeights().stream()
                .map(cw -> cw.categoryId())
                .toList();
        if (categoryIds.isEmpty()) return Map.of();
        return categoryRepository.findAllByIdsAndTenantId(categoryIds, tenantId)
                .stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }

    private List<RankingEntryResult> buildRanking(List<Candidate> candidates,
                                                   ScoringConfig scoringConfig,
                                                   Map<UUID, String> categoryNames) {
        List<Candidate> responded = candidates.stream()
                .filter(c -> c.getScores() != null)
                .sorted(Comparator.comparingDouble((Candidate c) -> c.getScores().total()).reversed())
                .toList();

        List<Candidate> pending = candidates.stream()
                .filter(c -> c.getScores() == null)
                .toList();

        List<RankingEntryResult> entries = new ArrayList<>(candidates.size());

        for (int i = 0; i < responded.size(); i++) {
            Candidate c = responded.get(i);
            entries.add(toEntry(c, i + 1, scoringConfig, categoryNames));
        }
        for (Candidate c : pending) {
            entries.add(toEntry(c, null, null, Map.of()));
        }
        return entries;
    }

    private RankingEntryResult toEntry(Candidate c, Integer rank, ScoringConfig config,
                                        Map<UUID, String> categoryNames) {
        Double totalScore = c.getScores() != null ? c.getScores().total() : null;
        CandidateClassification classification = classify(totalScore, config);
        Map<String, Double> scoresByCategory = resolveScoresByCategory(c, categoryNames);

        return new RankingEntryResult(
                c.getId(), c.getName(), c.getEmail(), c.getToken(),
                c.getStatus().name(), c.getResponseId(),
                rank, totalScore, classification, scoresByCategory, c.getRespondedAt()
        );
    }

    private CandidateClassification classify(Double score, ScoringConfig config) {
        if (score == null || config == null) return null;
        if (score >= config.aptoMin())    return CandidateClassification.APTO;
        if (score >= config.revisarMin()) return CandidateClassification.REVISAR;
        return CandidateClassification.NO_APTO;
    }

    private Map<String, Double> resolveScoresByCategory(Candidate c, Map<UUID, String> categoryNames) {
        if (c.getScores() == null || c.getScores().byCategory() == null) return Map.of();
        return c.getScores().byCategory().entrySet().stream()
                .filter(e -> categoryNames.containsKey(e.getKey()))
                .collect(Collectors.toMap(
                        e -> categoryNames.get(e.getKey()),
                        Map.Entry::getValue
                ));
    }
}
