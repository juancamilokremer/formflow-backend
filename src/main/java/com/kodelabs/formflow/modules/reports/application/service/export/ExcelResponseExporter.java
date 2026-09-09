package com.kodelabs.formflow.modules.reports.application.service.export;

import com.kodelabs.formflow.modules.reports.domain.model.ExportFormat;
import com.kodelabs.formflow.modules.reports.domain.port.in.result.ExportResult;
import com.kodelabs.formflow.shared.export.ExcelRowWriter;
import com.kodelabs.formflow.shared.export.ExportFilenames;
import com.kodelabs.formflow.shared.i18n.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExcelResponseExporter implements ResponseExporter {

    private static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final Messages messages;
    private final ExcelRowWriter excelRowWriter;

    @Override
    public ExportFormat format() {
        return ExportFormat.EXCEL;
    }

    @Override
    public ExportResult export(String formName, List<List<String>> rows) {
        byte[] content = excelRowWriter.write(messages.get("export.excel.sheet_name"), rows);
        return new ExportResult(content, ExportFilenames.build(formName, "xlsx"), CONTENT_TYPE);
    }
}
