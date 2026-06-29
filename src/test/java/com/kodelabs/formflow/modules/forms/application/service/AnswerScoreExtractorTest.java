package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.config.AnswerOption;
import com.kodelabs.formflow.modules.forms.domain.model.config.MultipleConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.ScaleConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.ScaleOption;
import com.kodelabs.formflow.modules.forms.domain.model.config.SingleConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.TextConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AnswerScoreExtractorTest {

    private AnswerScoreExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new AnswerScoreExtractor();
    }

    // ---- SINGLE ----

    @Test
    void single_returnsScoreOfMatchingOptionId() {
        AnswerOption opt = AnswerOption.builder().id("opt-1").label("3-5 años").score(7).build();
        FormQuestion q = questionWith(SingleConfig.builder().options(List.of(opt)).build());

        assertThat(extractor.extractScore(q, "opt-1")).isEqualTo(7);
    }

    @Test
    void single_optionIdNotFound_returnsZero() {
        AnswerOption opt = AnswerOption.builder().id("opt-1").score(7).build();
        FormQuestion q = questionWith(SingleConfig.builder().options(List.of(opt)).build());

        assertThat(extractor.extractScore(q, "opt-unknown")).isZero();
    }

    // ---- MULTIPLE ----

    @Test
    void multiple_sumsScoressOfAllSelectedOptionIds() {
        AnswerOption o1 = AnswerOption.builder().id("a").score(5).build();
        AnswerOption o2 = AnswerOption.builder().id("b").score(3).build();
        AnswerOption o3 = AnswerOption.builder().id("c").score(2).build();
        FormQuestion q = questionWith(MultipleConfig.builder().options(List.of(o1, o2, o3)).build());

        assertThat(extractor.extractScore(q, List.of("a", "c"))).isEqualTo(7);
    }

    @Test
    void multiple_noMatchingOptions_returnsZero() {
        AnswerOption o1 = AnswerOption.builder().id("a").score(5).build();
        FormQuestion q = questionWith(MultipleConfig.builder().options(List.of(o1)).build());

        assertThat(extractor.extractScore(q, List.of("x", "y"))).isZero();
    }

    // ---- SCALE ----

    @Test
    void scale_returnsScoreOfMatchingValue() {
        ScaleConfig config = ScaleConfig.builder()
                .options(List.of(new ScaleOption(3, 6), new ScaleOption(5, 10)))
                .build();
        FormQuestion q = questionWith(config);

        assertThat(extractor.extractScore(q, 3)).isEqualTo(6);
    }

    @Test
    void scale_acceptsDoubleValueAsInt() {
        ScaleConfig config = ScaleConfig.builder()
                .options(List.of(new ScaleOption(4, 8)))
                .build();
        FormQuestion q = questionWith(config);

        assertThat(extractor.extractScore(q, 4.0)).isEqualTo(8);
    }

    @Test
    void scale_valueNotFound_returnsZero() {
        ScaleConfig config = ScaleConfig.builder()
                .options(List.of(new ScaleOption(1, 2)))
                .build();
        FormQuestion q = questionWith(config);

        assertThat(extractor.extractScore(q, 9)).isZero();
    }

    // ---- EDGE CASES ----

    @Test
    void nullValue_returnsZero() {
        FormQuestion q = questionWith(SingleConfig.builder().build());

        assertThat(extractor.extractScore(q, null)).isZero();
    }

    @Test
    void nullConfig_returnsZero() {
        FormQuestion q = FormQuestion.builder().id(UUID.randomUUID()).config(null).build();

        assertThat(extractor.extractScore(q, "anything")).isZero();
    }

    @Test
    void unsupportedConfigType_returnsZero() {
        FormQuestion q = questionWith(new TextConfig());

        assertThat(extractor.extractScore(q, "some text")).isZero();
    }

    private FormQuestion questionWith(com.kodelabs.formflow.modules.forms.domain.model.config.QuestionConfig config) {
        return FormQuestion.builder().id(UUID.randomUUID()).config(config).build();
    }
}
