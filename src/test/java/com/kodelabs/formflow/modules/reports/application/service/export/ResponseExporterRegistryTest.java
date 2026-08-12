package com.kodelabs.formflow.modules.reports.application.service.export;

import com.kodelabs.formflow.modules.reports.domain.model.ExportFormat;
import com.kodelabs.formflow.modules.reports.domain.port.in.result.ExportResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResponseExporterRegistryTest {

    private static ResponseExporter exporterFor(ExportFormat format) {
        return new ResponseExporter() {
            @Override
            public ExportFormat format() {
                return format;
            }

            @Override
            public ExportResult export(String formName, List<List<String>> rows) {
                return new ExportResult(new byte[0], "file", "text/plain");
            }
        };
    }

    @Test
    void findReturnsTheExporterRegisteredForThatFormat() {
        ResponseExporter excel = exporterFor(ExportFormat.EXCEL);
        ResponseExporterRegistry registry = new ResponseExporterRegistry(List.of(excel, exporterFor(ExportFormat.CSV)));

        assertThat(registry.find(ExportFormat.EXCEL)).contains(excel);
    }

    @Test
    void findReturnsEmptyWhenNoExporterIsRegisteredForThatFormat() {
        ResponseExporterRegistry registry = new ResponseExporterRegistry(List.of(exporterFor(ExportFormat.EXCEL)));

        assertThat(registry.find(ExportFormat.CSV)).isEmpty();
    }

    @Test
    void constructorThrowsOnDuplicateFormat() {
        List<ResponseExporter> duplicated = List.of(exporterFor(ExportFormat.EXCEL), exporterFor(ExportFormat.EXCEL));

        assertThatThrownBy(() -> new ResponseExporterRegistry(duplicated))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("EXCEL");
    }
}
