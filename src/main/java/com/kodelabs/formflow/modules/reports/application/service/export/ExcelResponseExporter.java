package com.kodelabs.formflow.modules.reports.application.service.export;

import com.kodelabs.formflow.modules.reports.domain.model.ExportFormat;
import com.kodelabs.formflow.modules.reports.domain.port.in.result.ExportResult;
import com.kodelabs.formflow.shared.i18n.Messages;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExcelResponseExporter implements ResponseExporter {

    private static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final Messages messages;

    @Override
    public ExportFormat format() {
        return ExportFormat.EXCEL;
    }

    @Override
    public ExportResult export(String formName, List<List<String>> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            writeRows(workbook, rows);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ExportResult(out.toByteArray(), ExportFilenames.build(formName, "xlsx"), CONTENT_TYPE);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build Excel export", e);
        }
    }

    private void writeRows(XSSFWorkbook workbook, List<List<String>> rows) {
        Sheet sheet = workbook.createSheet(messages.get("export.excel.sheet_name"));
        CellStyle headerStyle = headerStyle(workbook);

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Row row = sheet.createRow(rowIndex);
            List<String> cells = rows.get(rowIndex);
            for (int colIndex = 0; colIndex < cells.size(); colIndex++) {
                Cell cell = row.createCell(colIndex);
                cell.setCellValue(cells.get(colIndex));
                if (rowIndex == 0) cell.setCellStyle(headerStyle);
            }
        }

        if (!rows.isEmpty()) {
            for (int col = 0; col < rows.get(0).size(); col++) {
                sheet.autoSizeColumn(col);
            }
        }
    }

    private CellStyle headerStyle(XSSFWorkbook workbook) {
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(boldFont);
        return style;
    }
}
