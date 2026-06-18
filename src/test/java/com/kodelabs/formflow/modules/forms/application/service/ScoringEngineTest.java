package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.config.AnswerOption;
import com.kodelabs.formflow.modules.forms.domain.model.config.SingleConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ScoringEngineTest {

    private ScoringEngine engine;

    private UUID catA;
    private UUID catB;
    private UUID q1Id;
    private UUID q2Id;

    @BeforeEach
    void setUp() {
        engine = new ScoringEngine();
        catA = UUID.randomUUID();
        catB = UUID.randomUUID();
        q1Id = UUID.randomUUID();
        q2Id = UUID.randomUUID();
    }

    @Test
    void calculatesContributionProportionalToWeightAndObtainedScore() {
        FormQuestion q1 = questionWithSingleConfig(q1Id, catA, 10);

        ScoringInput input = new ScoringInput(
                List.of(q1),
                Map.of(q1Id, 5),
                Map.of(catA, 100.0));

        ScoringResult result = engine.calculate(input);

        // obtained 5 / max 10 * weight 100 = 50
        assertThat(result.totalScore()).isCloseTo(50.0, within(0.001));
        assertThat(result.scoresByCategory().get(catA).contribution()).isCloseTo(50.0, within(0.001));
    }

    @Test
    void sumsCategoryContributionsAcrossMultipleCategories() {
        FormQuestion q1 = questionWithSingleConfig(q1Id, catA, 10);
        FormQuestion q2 = questionWithSingleConfig(q2Id, catB, 10);

        ScoringInput input = new ScoringInput(
                List.of(q1, q2),
                Map.of(q1Id, 10, q2Id, 10),
                Map.of(catA, 60.0, catB, 40.0));

        ScoringResult result = engine.calculate(input);

        assertThat(result.totalScore()).isCloseTo(100.0, within(0.001));
    }

    @Test
    void excludesQuestionsWithNoCategoryFromScoring() {
        FormQuestion withCategory = questionWithSingleConfig(q1Id, catA, 10);
        FormQuestion withoutCategory = FormQuestion.builder()
                .id(q2Id).categoryId(null)
                .config(SingleConfig.builder()
                        .options(List.of(AnswerOption.builder().score(10).build()))
                        .build())
                .build();

        ScoringInput input = new ScoringInput(
                List.of(withCategory, withoutCategory),
                Map.of(q1Id, 10, q2Id, 10),
                Map.of(catA, 100.0));

        ScoringResult result = engine.calculate(input);

        assertThat(result.totalScore()).isCloseTo(100.0, within(0.001));
    }

    @Test
    void excludesQuestionsWhoseCategoryHasNoWeight() {
        UUID unweightedCat = UUID.randomUUID();
        FormQuestion q = questionWithSingleConfig(q1Id, unweightedCat, 10);

        ScoringInput input = new ScoringInput(
                List.of(q),
                Map.of(q1Id, 10),
                Map.of(catA, 100.0));

        ScoringResult result = engine.calculate(input);

        assertThat(result.totalScore()).isCloseTo(0.0, within(0.001));
    }

    @Test
    void givesZeroContributionWhenCategoryMaxScoreIsZero() {
        FormQuestion q = FormQuestion.builder()
                .id(q1Id).categoryId(catA).config(null)
                .build();

        ScoringInput input = new ScoringInput(
                List.of(q),
                Map.of(q1Id, 0),
                Map.of(catA, 100.0));

        ScoringResult result = engine.calculate(input);

        assertThat(result.scoresByCategory().get(catA).contribution()).isZero();
    }

    @Test
    void capsTotalScoreAt100() {
        FormQuestion q1 = questionWithSingleConfig(q1Id, catA, 10);
        FormQuestion q2 = questionWithSingleConfig(q2Id, catB, 10);

        ScoringInput input = new ScoringInput(
                List.of(q1, q2),
                Map.of(q1Id, 10, q2Id, 10),
                Map.of(catA, 80.0, catB, 80.0));

        ScoringResult result = engine.calculate(input);

        assertThat(result.totalScore()).isLessThanOrEqualTo(100.0);
    }

    private FormQuestion questionWithSingleConfig(UUID id, UUID categoryId, int maxOptionScore) {
        return FormQuestion.builder()
                .id(id).categoryId(categoryId)
                .config(SingleConfig.builder()
                        .options(List.of(AnswerOption.builder().score(maxOptionScore).build()))
                        .build())
                .build();
    }
}
