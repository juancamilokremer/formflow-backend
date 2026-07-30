package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.CandidateClassifier;
import com.kodelabs.formflow.modules.forms.domain.model.Category;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateClassification;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateFormScore;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetRankingUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetRankingQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.RankingEntryResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.RankingFormScoreResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
    private final FormRepositoryPort formRepository;
    private final CandidateClassifier candidateClassifier;

    @Override
    @Transactional(readOnly = true)
    public List<RankingEntryResult> execute(GetRankingQuery query) {
        Convocatoria convocatoria = loadConvocatoria(query);
        List<Candidate> candidates = candidateRepository.findAllByConvocatoriaId(query.convocatoriaId());
        Map<UUID, String> categoryNames = loadCategoryNames(convocatoria, query.tenantId());
        Map<UUID, String> formNames = loadFormNames(convocatoria, query.tenantId());
        return buildRanking(candidates, convocatoria, categoryNames, formNames);
    }

    private Convocatoria loadConvocatoria(GetRankingQuery query) {
        return convocatoriaRepository.findByIdAndTenantId(query.convocatoriaId(), query.tenantId())
                .orElseThrow(() -> new BusinessException("error.convocatoria.not_found",
                        HttpStatus.NOT_FOUND, query.convocatoriaId()));
    }

    private Map<UUID, String> loadCategoryNames(Convocatoria convocatoria, UUID tenantId) {
        List<UUID> categoryIds = convocatoria.getForms().stream()
                .flatMap(f -> f.getCategoryWeights().stream())
                .map(cw -> cw.categoryId())
                .distinct()
                .toList();
        if (categoryIds.isEmpty()) return Map.of();
        return categoryRepository.findAllByIdsAndTenantId(categoryIds, tenantId)
                .stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }

    private Map<UUID, String> loadFormNames(Convocatoria convocatoria, UUID tenantId) {
        Map<UUID, String> names = new HashMap<>();
        for (ConvocatoriaForm cf : convocatoria.getForms()) {
            formRepository.findByIdAndTenantId(cf.getFormId(), tenantId)
                    .ifPresent(f -> names.put(f.getId(), f.getName()));
        }
        return names;
    }

    private List<RankingEntryResult> buildRanking(List<Candidate> candidates,
                                                   Convocatoria convocatoria,
                                                   Map<UUID, String> categoryNames,
                                                   Map<UUID, String> formNames) {
        List<Candidate> responded = candidates.stream()
                .filter(c -> c.getScores() != null && c.getScores().total() != null)
                .sorted(Comparator.comparingDouble((Candidate c) -> c.getScores().total()).reversed())
                .toList();

        List<Candidate> pending = candidates.stream()
                .filter(c -> c.getScores() == null || c.getScores().total() == null)
                .toList();

        List<RankingEntryResult> entries = new ArrayList<>(candidates.size());

        for (int i = 0; i < responded.size(); i++) {
            Candidate c = responded.get(i);
            entries.add(toEntry(c, i + 1, convocatoria, categoryNames, formNames));
        }
        for (Candidate c : pending) {
            entries.add(toEntry(c, null, convocatoria, categoryNames, formNames));
        }
        return entries;
    }

    private RankingEntryResult toEntry(Candidate c, Integer rank, Convocatoria convocatoria,
                                        Map<UUID, String> categoryNames, Map<UUID, String> formNames) {
        Double totalScore = c.getScores() != null ? c.getScores().total() : null;
        CandidateClassification classification = candidateClassifier.classify(
                c.getScores(), convocatoria.getForms(), convocatoria.getScoringConfig());
        Map<String, Double> scoresByCategory = resolveScoresByCategory(c, categoryNames);
        List<RankingFormScoreResult> formScores = buildFormScores(c, convocatoria, formNames);

        return new RankingEntryResult(
                c.getId(), c.getName(), c.getEmail(), c.getToken(),
                c.getStatus().name(), c.getResponseId(),
                rank, totalScore, classification, scoresByCategory, c.getRespondedAt(),
                formScores
        );
    }

    private Map<String, Double> resolveScoresByCategory(Candidate c, Map<UUID, String> categoryNames) {
        if (c.getScores() == null || c.getScores().perForm() == null) return Map.of();
        return c.getScores().perForm().stream()
                .flatMap(fs -> fs.byCategory().entrySet().stream())
                .filter(e -> categoryNames.containsKey(e.getKey()))
                .collect(Collectors.toMap(
                        e -> categoryNames.get(e.getKey()),
                        Map.Entry::getValue,
                        (a, b) -> a
                ));
    }

    private List<RankingFormScoreResult> buildFormScores(Candidate c, Convocatoria convocatoria,
                                                           Map<UUID, String> formNames) {
        return convocatoria.getForms().stream()
                .sorted(Comparator.comparingInt(ConvocatoriaForm::getPosition))
                .map(cf -> {
                    CandidateFormScore fs = findFormScore(c, cf.getId());
                    return new RankingFormScoreResult(
                            cf.getFormId(), formNames.get(cf.getFormId()), cf.getWeight(),
                            fs != null ? fs.total() : null, fs != null);
                })
                .toList();
    }

    private CandidateFormScore findFormScore(Candidate c, UUID convocatoriaFormId) {
        if (c.getScores() == null || c.getScores().perForm() == null) return null;
        return c.getScores().perForm().stream()
                .filter(fs -> fs.convocatoriaFormId().equals(convocatoriaFormId))
                .findFirst()
                .orElse(null);
    }
}
