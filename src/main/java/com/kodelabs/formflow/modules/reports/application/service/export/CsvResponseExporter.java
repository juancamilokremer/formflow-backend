package com.kodelabs.formflow.modules.reports.application.service.export;

import com.kodelabs.formflow.modules.reports.domain.model.ExportFormat;
import com.kodelabs.formflow.modules.reports.domain.port.in.result.ExportResult;
import com.kodelabs.formflow.shared.export.ExportFilenames;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CsvResponseExporter implements ResponseExporter {

    private static final String CONTENT_TYPE = "text/csv; charset=UTF-8";

    /**
     * Excel on Windows opens a .csv with the system ANSI codepage unless the file starts with
     * a BOM, which turns every accented character into mojibake ("envío" reads as "envÃ­o").
     * Since these exports are opened in Excel far more often than piped into a parser, the
     * BOM goes in; modern readers strip it.
     */
    private static final byte[] UTF8_BOM = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

    @Override
    public ExportFormat format() {
        return ExportFormat.CSV;
    }

    @Override
    public ExportResult export(String formName, List<List<String>> rows) {
        String csv = rows.stream()
                .map(this::toCsvLine)
                .collect(Collectors.joining("\r\n"));
        byte[] body = csv.getBytes(StandardCharsets.UTF_8);
        byte[] content = new byte[UTF8_BOM.length + body.length];
        System.arraycopy(UTF8_BOM, 0, content, 0, UTF8_BOM.length);
        System.arraycopy(body, 0, content, UTF8_BOM.length, body.length);

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
