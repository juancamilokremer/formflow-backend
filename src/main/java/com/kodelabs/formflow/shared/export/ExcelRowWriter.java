package com.kodelabs.formflow.shared.export;

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

/**
 * Generic Excel engine: writes a matrix of strings (row 0 = header, bolded) per sheet to a
 * .xlsx workbook. Has no knowledge of what the rows represent — every Excel export in the app
 * should go through this instead of writing POI cells again.
 */
@Component
public class ExcelRowWriter {

    public byte[] write(String sheetName, List<List<String>> rows) {
        return write(List.of(new ExcelSheet(sheetName, rows)));
    }

    public byte[] write(List<ExcelSheet> sheets) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            for (ExcelSheet sheet : sheets) {
                writeRows(workbook, sheet.name(), sheet.rows());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build Excel export", e);
        }
    }

    private void writeRows(XSSFWorkbook workbook, String sheetName, List<List<String>> rows) {
        Sheet sheet = workbook.createSheet(sheetName);
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
