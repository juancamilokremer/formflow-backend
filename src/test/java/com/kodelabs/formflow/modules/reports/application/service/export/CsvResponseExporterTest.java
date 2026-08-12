package com.kodelabs.formflow.modules.reports.application.service.export;

import com.kodelabs.formflow.modules.reports.domain.model.ExportFormat;
import com.kodelabs.formflow.modules.reports.domain.port.in.result.ExportResult;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvResponseExporterTest {

    private final CsvResponseExporter exporter = new CsvResponseExporter();

    @Test
    void formatIsCsv() {
        assertThat(exporter.format()).isEqualTo(ExportFormat.CSV);
    }

    @Test
    void joinsCellsWithCommasAndRowsWithCrlf() {
        ExportResult result = exporter.export("Encuesta",
                List.of(List.of("Fecha de envío", "Nombre"), List.of("2026-08-01 10:00", "Ana")));

        assertThat(new String(result.content(), StandardCharsets.UTF_8))
                .isEqualTo("Fecha de envío,Nombre\r\n2026-08-01 10:00,Ana");
    }

    @Test
    void quotesCellsContainingACommaAndEscapesInternalQuotes() {
        ExportResult result = exporter.export("Encuesta",
                List.of(List.of("Comentarios"), List.of("Bueno, con \"comillas\"")));

        assertThat(new String(result.content(), StandardCharsets.UTF_8))
                .isEqualTo("Comentarios\r\n\"Bueno, con \"\"comillas\"\"\"");
    }

    @Test
    void quotesCellsContainingANewline() {
        ExportResult result = exporter.export("Encuesta",
                List.of(List.of("Comentarios"), List.of("Linea uno\nLinea dos")));

        assertThat(new String(result.content(), StandardCharsets.UTF_8))
                .isEqualTo("Comentarios\r\n\"Linea uno\nLinea dos\"");
    }

    @Test
    void contentTypeIsTextCsv() {
        ExportResult result = exporter.export("Encuesta", List.of(List.of("a")));
        assertThat(result.contentType()).isEqualTo("text/csv");
    }

    @Test
    void filenameEndsWithCsvExtensionAndIsSlugified() {
        ExportResult result = exporter.export("Encuesta de Clima!", List.of(List.of("a")));
        assertThat(result.filename()).matches("encuesta-de-clima_\\d{8}\\.csv");
    }
}
