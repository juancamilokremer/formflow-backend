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

        assertThat(bodyOf(result))
                .isEqualTo("Fecha de envío,Nombre\r\n2026-08-01 10:00,Ana");
    }

    @Test
    void quotesCellsContainingACommaAndEscapesInternalQuotes() {
        ExportResult result = exporter.export("Encuesta",
                List.of(List.of("Comentarios"), List.of("Bueno, con \"comillas\"")));

        assertThat(bodyOf(result))
                .isEqualTo("Comentarios\r\n\"Bueno, con \"\"comillas\"\"\"");
    }

    @Test
    void quotesCellsContainingANewline() {
        ExportResult result = exporter.export("Encuesta",
                List.of(List.of("Comentarios"), List.of("Linea uno\nLinea dos")));

        assertThat(bodyOf(result))
                .isEqualTo("Comentarios\r\n\"Linea uno\nLinea dos\"");
    }

    @Test
    void contentTypeDeclaresUtf8() {
        ExportResult result = exporter.export("Encuesta", List.of(List.of("a")));
        assertThat(result.contentType()).isEqualTo("text/csv; charset=UTF-8");
    }

    @Test
    void filenameEndsWithCsvExtensionAndIsSlugified() {
        ExportResult result = exporter.export("Encuesta de Clima!", List.of(List.of("a")));
        assertThat(result.filename()).matches("encuesta-de-clima_\\d{8}\\.csv");
    }

    @Test
    void startsWithAUtf8BomSoExcelReadsAccentsCorrectly() {
        ExportResult result = exporter.export("Encuesta", List.of(List.of("Fecha de envío")));

        // Without the BOM, Excel on Windows falls back to the ANSI codepage and "envío"
        // renders as "envÃ­o".
        assertThat(result.content()).startsWith((byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
        assertThat(bodyOf(result)).startsWith("Fecha de envío");
    }

    /** The payload without the BOM, so content assertions stay about the CSV itself. */
    private String bodyOf(ExportResult result) {
        byte[] content = result.content();
        return new String(content, 3, content.length - 3, StandardCharsets.UTF_8);
    }
}