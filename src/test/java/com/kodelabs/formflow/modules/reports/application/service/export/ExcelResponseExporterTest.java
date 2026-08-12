package com.kodelabs.formflow.modules.reports.application.service.export;

import com.kodelabs.formflow.modules.reports.domain.model.ExportFormat;
import com.kodelabs.formflow.modules.reports.domain.port.in.result.ExportResult;
import com.kodelabs.formflow.shared.i18n.Messages;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ExcelResponseExporterTest {

    @Mock private Messages messages;
    @InjectMocks private ExcelResponseExporter exporter;

    @BeforeEach
    void setUp() {
        lenient().when(messages.get("export.excel.sheet_name")).thenReturn("Respuestas");
    }

    @Test
    void formatIsExcel() {
        assertThat(exporter.format()).isEqualTo(ExportFormat.EXCEL);
    }

    @Test
    void writesOneSheetWithHeaderAndDataRows() throws IOException {
        ExportResult result = exporter.export("Encuesta",
                List.of(List.of("Fecha de envío", "Nombre"), List.of("2026-08-01 10:00", "Ana")));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result.content()))) {
            Sheet sheet = workbook.getSheet("Respuestas");
            assertThat(sheet).isNotNull();

            Row header = sheet.getRow(0);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Fecha de envío");
            assertThat(header.getCell(1).getStringCellValue()).isEqualTo("Nombre");
            int fontIndex = header.getCell(0).getCellStyle().getFontIndex();
            assertThat(workbook.getFontAt(fontIndex).getBold()).isTrue();

            Row dataRow = sheet.getRow(1);
            assertThat(dataRow.getCell(0).getStringCellValue()).isEqualTo("2026-08-01 10:00");
            assertThat(dataRow.getCell(1).getStringCellValue()).isEqualTo("Ana");
        }
    }

    @Test
    void contentTypeIsOpenXmlSpreadsheet() {
        ExportResult result = exporter.export("Encuesta", List.of(List.of("a")));
        assertThat(result.contentType())
                .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @Test
    void filenameEndsWithXlsxExtensionAndIsSlugified() {
        ExportResult result = exporter.export("Encuesta de Clima!", List.of(List.of("a")));
        assertThat(result.filename()).matches("encuesta-de-clima_\\d{8}\\.xlsx");
    }
}
