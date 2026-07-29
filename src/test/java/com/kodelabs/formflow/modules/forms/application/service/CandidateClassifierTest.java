package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateClassification;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateFormScore;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateScores;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ScoringConfig;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CandidateClassifierTest {

    private final CandidateClassifier classifier = new CandidateClassifier();
    private final ScoringConfig config = new ScoringConfig(70, 50);

    @Test
    void returnsNullWhenCandidateHasNotFinishedAllForms() {
        CandidateScores incomplete = new CandidateScores(null, List.of());
        assertThat(classifier.classify(incomplete, List.of(), config)).isNull();
    }

    @Test
    void returnsNullWhenScoresIsNull() {
        assertThat(classifier.classify(null, List.of(), config)).isNull();
    }

    @Test
    void classifiesAptoWhenTotalMeetsAptoMin() {
        CandidateScores scores = new CandidateScores(85.0, List.of());
        assertThat(classifier.classify(scores, List.of(), config)).isEqualTo(CandidateClassification.APTO);
    }

    @Test
    void classifiesRevisarWhenTotalBetweenThresholds() {
        CandidateScores scores = new CandidateScores(60.0, List.of());
        assertThat(classifier.classify(scores, List.of(), config)).isEqualTo(CandidateClassification.REVISAR);
    }

    @Test
    void classifiesNoAptoWhenTotalBelowRevisarMin() {
        CandidateScores scores = new CandidateScores(30.0, List.of());
        assertThat(classifier.classify(scores, List.of(), config)).isEqualTo(CandidateClassification.NO_APTO);
    }

    @Test
    void forcesNoAptoWhenAFormMinScoreIsMissed() {
        UUID convFormId = UUID.randomUUID();
        ConvocatoriaForm form = ConvocatoriaForm.builder().id(convFormId).weight(60).minScore(50).build();
        CandidateFormScore formScore = new CandidateFormScore(convFormId, UUID.randomUUID(), 38.0, Map.of());
        CandidateScores scores = new CandidateScores(86.0, List.of(formScore));

        assertThat(classifier.classify(scores, List.of(form), config)).isEqualTo(CandidateClassification.NO_APTO);
    }

    @Test
    void aptoWhenAllMinScoresAreMet() {
        UUID convFormId1 = UUID.randomUUID();
        UUID convFormId2 = UUID.randomUUID();
        ConvocatoriaForm form1 = ConvocatoriaForm.builder().id(convFormId1).weight(60).minScore(50).build();
        ConvocatoriaForm form2 = ConvocatoriaForm.builder().id(convFormId2).weight(40).build();
        CandidateScores scores = new CandidateScores(90.0, List.of(
                new CandidateFormScore(convFormId1, UUID.randomUUID(), 91.0, Map.of()),
                new CandidateFormScore(convFormId2, UUID.randomUUID(), 78.0, Map.of())));

        assertThat(classifier.classify(scores, List.of(form1, form2), config)).isEqualTo(CandidateClassification.APTO);
    }
}
