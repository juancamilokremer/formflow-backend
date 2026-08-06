package com.kodelabs.formflow.modules.forms.application.service.stats;

import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionStatsResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TextStatsCalculatorTest {

    private final TextStatsCalculator calculator = new TextStatsCalculator();

    private FormQuestion question() {
        return FormQuestion.builder().id(UUID.randomUUID()).title("Comentarios").build();
    }

    @Test
    void countsOnlyNonBlankAnswers() {
        QuestionStatsResult result = calculator.calculate(
                question(), 4, List.of("Excelente", "  ", "", "Muy bueno"));

        assertThat(result.answeredCount()).isEqualTo(2);
    }

    @Test
    void takesAtMostTheFirstThreeAnswersAsSample() {
        QuestionStatsResult result = calculator.calculate(
                question(), 5, List.of("uno", "dos", "tres", "cuatro", "cinco"));

        assertThat(result.sampleAnswers()).containsExactly("uno", "dos", "tres");
    }

    @Test
    void truncatesSampleAnswersToOneHundredCharacters() {
        String longAnswer = "a".repeat(150);

        QuestionStatsResult result = calculator.calculate(question(), 1, List.of(longAnswer));

        assertThat(result.sampleAnswers()).hasSize(1);
        assertThat(result.sampleAnswers().get(0)).hasSize(100);
    }

    @Test
    void returnsEmptySampleWhenThereAreNoAnswers() {
        QuestionStatsResult result = calculator.calculate(question(), 0, List.of());

        assertThat(result.sampleAnswers()).isEmpty();
        assertThat(result.answeredCount()).isZero();
    }
}
