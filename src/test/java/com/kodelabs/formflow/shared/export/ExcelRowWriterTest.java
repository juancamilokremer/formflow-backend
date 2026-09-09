package com.kodelabs.formflow.shared.export;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelRowWriterTest {

    private final ExcelRowWriter writer = new ExcelRowWriter();

    @Test
    void writesOneSheetWithHeaderBoldAndDataRows() throws IOException {
        byte[] content = writer.write("Mi hoja",
                List.of(List.of("Columna A", "Columna B"), List.of("valor 1", "valor 2")));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            Sheet sheet = workbook.getSheet("Mi hoja");
            assertThat(sheet).isNotNull();

            Row header = sheet.getRow(0);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Columna A");
            assertThat(header.getCell(1).getStringCellValue()).isEqualTo("Columna B");
            int fontIndex = header.getCell(0).getCellStyle().getFontIndex();
            assertThat(workbook.getFontAt(fontIndex).getBold()).isTrue();

            Row dataRow = sheet.getRow(1);
            assertThat(dataRow.getCell(0).getStringCellValue()).isEqualTo("valor 1");
            assertThat(dataRow.getCell(1).getStringCellValue()).isEqualTo("valor 2");
        }
    }

    @Test
    void writesOnlyAHeaderWhenThereAreNoDataRows() throws IOException {
        byte[] content = writer.write("Vacía", List.of(List.of("Solo header")));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            Sheet sheet = workbook.getSheet("Vacía");
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(1);
        }
    }
}
