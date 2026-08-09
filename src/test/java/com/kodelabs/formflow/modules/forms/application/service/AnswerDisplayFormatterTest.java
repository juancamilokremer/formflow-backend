package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.snapshot.QuestionSnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AnswerDisplayFormatterTest {

    private final AnswerDisplayFormatter formatter = new AnswerDisplayFormatter();

    private QuestionSnapshot question(String type, Map<String, Object> config) {
        return new QuestionSnapshot(UUID.randomUUID(), "Pregunta", null, type, 0, false, null, null, config);
    }

    @Test
    void returnsNullWhenValueIsNull() {
        assertThat(formatter.format(question("text", Map.of()), null)).isNull();
    }

    @Test
    void passesThroughTextScaleAndNpsValuesAsIs() {
        assertThat(formatter.format(question("text", Map.of()), "Muy bueno")).isEqualTo("Muy bueno");
        assertThat(formatter.format(question("scale", Map.of()), 4)).isEqualTo("4");
        assertThat(formatter.format(question("nps", Map.of()), 9)).isEqualTo("9");
        assertThat(formatter.format(question("date", Map.of()), "2026-08-01")).isEqualTo("2026-08-01");
        assertThat(formatter.format(question("file", Map.of()), "https://files/a.pdf")).isEqualTo("https://files/a.pdf");
    }

    @Test
    void resolvesOptionLabelForSingleQuestion() {
        Map<String, Object> config = Map.of("options", List.of(
                Map.of("id", "opt-1", "label", "Sí", "score", 10),
                Map.of("id", "opt-2", "label", "No", "score", 0)));

        assertThat(formatter.format(question("single", config), "opt-1")).isEqualTo("Sí");
    }

    @Test
    void fallsBackToRawIdWhenSingleOptionNoLongerExistsInSnapshot() {
        Map<String, Object> config = Map.of("options", List.of(Map.of("id", "opt-1", "label", "Sí", "score", 10)));

        assertThat(formatter.format(question("single", config), "opt-missing")).isEqualTo("opt-missing");
    }

    @Test
    void resolvesAndJoinsMultipleOptionLabels() {
        Map<String, Object> config = Map.of("options", List.of(
                Map.of("id", "opt-1", "label", "Java", "score", 5),
                Map.of("id", "opt-2", "label", "Python", "score", 5),
                Map.of("id", "opt-3", "label", "Go", "score", 5)));

        assertThat(formatter.format(question("multiple", config), List.of("opt-1", "opt-3")))
                .isEqualTo("Java, Go");
    }

    @Test
    void resolvesMatrixRowsToRowLabelColonColumnLabel() {
        Map<String, Object> config = Map.of(
                "rows", List.of(
                        Map.of("id", "row-1", "label", "Puntualidad"),
                        Map.of("id", "row-2", "label", "Comunicación")),
                "columns", List.of(
                        Map.of("id", "col-1", "label", "Malo", "score", 0),
                        Map.of("id", "col-2", "label", "Bueno", "score", 5)));
        Map<String, Object> value = Map.of("row-1", "col-2", "row-2", "col-1");

        String result = formatter.format(question("matrix", config), value);

        assertThat(result).contains("Puntualidad: Bueno").contains("Comunicación: Malo");
    }

    @Test
    void skipsMatrixRowsWithNoAnswer() {
        Map<String, Object> config = Map.of(
                "rows", List.of(Map.of("id", "row-1", "label", "Puntualidad")),
                "columns", List.of(Map.of("id", "col-1", "label", "Bueno", "score", 5)));

        assertThat(formatter.format(question("matrix", config), Map.of())).isEmpty();
    }
}
