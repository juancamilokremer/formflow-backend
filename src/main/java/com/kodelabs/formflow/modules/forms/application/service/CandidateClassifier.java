package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateClassification;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateFormScore;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateScores;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ScoringConfig;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CandidateClassifier {

    public CandidateClassification classify(CandidateScores scores, List<ConvocatoriaForm> forms, ScoringConfig config) {
        if (scores == null || scores.total() == null || config == null) return null;

        boolean anyMinScoreMissed = forms.stream().anyMatch(f -> f.getMinScore() != null
                && findFormScore(scores, f.getId())
                        .map(s -> s.total() < f.getMinScore())
                        .orElse(true));
        if (anyMinScoreMissed) return CandidateClassification.NO_APTO;

        double total = scores.total();
        if (total >= config.aptoMin())    return CandidateClassification.APTO;
        if (total >= config.revisarMin()) return CandidateClassification.REVISAR;
        return CandidateClassification.NO_APTO;
    }

    private Optional<CandidateFormScore> findFormScore(CandidateScores scores, UUID convocatoriaFormId) {
        if (scores.perForm() == null) return Optional.empty();
        return scores.perForm().stream()
                .filter(s -> s.convocatoriaFormId().equals(convocatoriaFormId))
                .findFirst();
    }
}
