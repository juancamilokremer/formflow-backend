package com.kodelabs.formflow.modules.reports.application.service.export;

import com.kodelabs.formflow.modules.reports.domain.model.ExportFormat;
import com.kodelabs.formflow.modules.reports.domain.port.in.result.ExportResult;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CsvResponseExporter implements ResponseExporter {

    private static final String CONTENT_TYPE = "text/csv";

    @Override
    public ExportFormat format() {
        return ExportFormat.CSV;
    }

    @Override
    public ExportResult export(String formName, List<List<String>> rows) {
        String csv = rows.stream()
                .map(this::toCsvLine)
                .collect(Collectors.joining("\r\n"));
        byte[] content = csv.getBytes(StandardCharsets.UTF_8);
        return new ExportResult(content, ExportFilenames.build(formName, "csv"), CONTENT_TYPE);
    }

    private String toCsvLine(List<String> cells) {
        return cells.stream().map(this::escape).collect(Collectors.joining(","));
    }

    private String escape(String value) {
        if (value == null || value.isEmpty()) return "";
        boolean needsQuoting = value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return needsQuoting ? "\"" + escaped + "\"" : escaped;
    }
}
