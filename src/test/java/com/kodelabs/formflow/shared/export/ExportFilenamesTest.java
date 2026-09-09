package com.kodelabs.formflow.shared.export;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExportFilenamesTest {

    @Test
    void slugifiesTheNameAndAppendsDateAndExtension() {
        String filename = ExportFilenames.build("Encuesta de Clima!", "xlsx");

        assertThat(filename).matches("encuesta-de-clima_\\d{8}\\.xlsx");
    }

    @Test
    void collapsesConsecutiveNonAlphanumericCharsIntoOneDash() {
        String filename = ExportFilenames.build("María   Gómez -- Reporte", "pdf");

        assertThat(filename).startsWith("mar-a-g-mez-reporte_");
    }

    @Test
    void trimsLeadingAndTrailingDashes() {
        String filename = ExportFilenames.build("¡Hola!", "csv");

        assertThat(filename).startsWith("hola_").doesNotContain("--");
    }
}
