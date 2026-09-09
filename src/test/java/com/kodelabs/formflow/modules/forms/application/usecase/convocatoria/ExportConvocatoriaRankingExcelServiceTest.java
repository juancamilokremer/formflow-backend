package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateClassification;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetRankingUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ExportConvocatoriaRankingQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetRankingQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ExportConvocatoriaRankingResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.RankingEntryResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.RankingFormScoreResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import com.kodelabs.formflow.shared.export.ExcelRowWriter;
import com.kodelabs.formflow.shared.i18n.Messages;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportConvocatoriaRankingExcelServiceTest {

    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private GetRankingUseCase getRanking;
    @Mock private Messages messages;
    @Spy private ExcelRowWriter excelRowWriter = new ExcelRowWriter();
    @InjectMocks private ExportConvocatoriaRankingExcelService service;

    private final UUID convId = UUID.randomUUID();
    private final UUID tenantId = UUID.randomUUID();
    private final UUID formId = UUID.randomUUID();

    private void stubMessages() {
        lenient().when(messages.get("export.excel.ranking_sheet_name")).thenReturn("Candidatos");
        lenient().when(messages.get("export.ranking.header.name")).thenReturn("Nombre");
        lenient().when(messages.get("export.ranking.header.email")).thenReturn("Email");
        lenient().when(messages.get("export.ranking.header.status")).thenReturn("Estado");
        lenient().when(messages.get("export.ranking.header.rank")).thenReturn("Rank");
        lenient().when(messages.get("export.ranking.header.total_score")).thenReturn("Puntaje total");
        lenient().when(messages.get("export.ranking.header.classification")).thenReturn("Clasificación");
    }

    @Test
    void exportsAllCandidatesWithCategoryAndFormColumnsWhenNoSelectionGiven() throws IOException {
        stubMessages();
        Convocatoria convocatoria = draftConvocatoria();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(convocatoria));

        UUID candidate1 = UUID.randomUUID();
        UUID candidate2 = UUID.randomUUID();
        List<RankingFormScoreResult> formScores = List.of(new RankingFormScoreResult(formId, "Evaluación técnica", 100, 85.0, true));
        RankingEntryResult entry1 = new RankingEntryResult(
                candidate1, "Ana Torres", "ana@test.com", UUID.randomUUID(), "RESPONDED", UUID.randomUUID(),
                1, 85.0, CandidateClassification.APTO, Map.of("Técnicas", 34.0), null, formScores);
        RankingEntryResult entry2 = new RankingEntryResult(
                candidate2, "Bruno Diaz", "bruno@test.com", UUID.randomUUID(), "INVITED", null,
                null, null, null, Map.of(), null, formScores);
        when(getRanking.execute(new GetRankingQuery(convId, tenantId))).thenReturn(List.of(entry1, entry2));

        ExportConvocatoriaRankingResult result = service.execute(
                new ExportConvocatoriaRankingQuery(convId, tenantId, null));

        assertThat(result.filename()).matches("analista-de-rrhh_\\d{8}\\.xlsx");

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result.content()))) {
            Sheet sheet = workbook.getSheet("Candidatos");
            Row header = sheet.getRow(0);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Nombre");
            assertThat(header.getCell(6).getStringCellValue()).isEqualTo("Técnicas");
            assertThat(header.getCell(7).getStringCellValue()).isEqualTo("Evaluación técnica");

            Row row1 = sheet.getRow(1);
            assertThat(row1.getCell(0).getStringCellValue()).isEqualTo("Ana Torres");
            assertThat(row1.getCell(3).getStringCellValue()).isEqualTo("1");
            assertThat(row1.getCell(4).getStringCellValue()).isEqualTo("85.0"); // Locale.ROOT, dot as decimal separator
            assertThat(row1.getCell(5).getStringCellValue()).isEqualTo("APTO");
            assertThat(row1.getCell(6).getStringCellValue()).isEqualTo("34.0");
            assertThat(row1.getCell(7).getStringCellValue()).isEqualTo("85.0");

            Row row2 = sheet.getRow(2);
            assertThat(row2.getCell(0).getStringCellValue()).isEqualTo("Bruno Diaz");
            assertThat(row2.getCell(3).getStringCellValue()).isEqualTo("");
            assertThat(row2.getCell(5).getStringCellValue()).isEqualTo("");
            assertThat(row2.getCell(6).getStringCellValue()).isEqualTo("");
        }
    }

    @Test
    void handlesACandidateWhoHasNotCompletedOneOfTheFormsYet() throws IOException {
        stubMessages();
        Convocatoria convocatoria = draftConvocatoria();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(convocatoria));

        UUID formBId = UUID.randomUUID();
        // Same shape GetRankingService.buildFormScores produces for an incomplete form: score=null, completed=false.
        List<RankingFormScoreResult> partialFormScores = List.of(
                new RankingFormScoreResult(formId, "Form A", 60, 70.0, true),
                new RankingFormScoreResult(formBId, "Form B", 40, null, false));
        RankingEntryResult entry = new RankingEntryResult(
                UUID.randomUUID(), "Carla Ruiz", "carla@test.com", UUID.randomUUID(), "IN_PROGRESS", UUID.randomUUID(),
                null, null, null, Map.of(), null, partialFormScores);
        when(getRanking.execute(new GetRankingQuery(convId, tenantId))).thenReturn(List.of(entry));

        ExportConvocatoriaRankingResult result = service.execute(
                new ExportConvocatoriaRankingQuery(convId, tenantId, null));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result.content()))) {
            Sheet sheet = workbook.getSheet("Candidatos");
            Row dataRow = sheet.getRow(1);
            assertThat(dataRow.getCell(6).getStringCellValue()).isEqualTo("70.0"); // Form A column
            assertThat(dataRow.getCell(7).getStringCellValue()).isEqualTo("");     // Form B column, not completed
        }
    }

    @Test
    void filtersBySelectionButKeepsOriginalRank() throws IOException {
        stubMessages();
        Convocatoria convocatoria = draftConvocatoria();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(convocatoria));

        UUID candidate1 = UUID.randomUUID();
        UUID candidate2 = UUID.randomUUID();
        RankingEntryResult entry1 = new RankingEntryResult(
                candidate1, "Ana Torres", "ana@test.com", UUID.randomUUID(), "RESPONDED", UUID.randomUUID(),
                1, 90.0, CandidateClassification.APTO, Map.of(), null, List.of());
        RankingEntryResult entry2 = new RankingEntryResult(
                candidate2, "Bruno Diaz", "bruno@test.com", UUID.randomUUID(), "RESPONDED", UUID.randomUUID(),
                2, 60.0, CandidateClassification.REVISAR, Map.of(), null, List.of());
        when(getRanking.execute(new GetRankingQuery(convId, tenantId))).thenReturn(List.of(entry1, entry2));

        ExportConvocatoriaRankingResult result = service.execute(
                new ExportConvocatoriaRankingQuery(convId, tenantId, List.of(candidate2)));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result.content()))) {
            Sheet sheet = workbook.getSheet("Candidatos");
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(2); // header + 1 selected candidate

            Row dataRow = sheet.getRow(1);
            assertThat(dataRow.getCell(0).getStringCellValue()).isEqualTo("Bruno Diaz");
            assertThat(dataRow.getCell(3).getStringCellValue()).isEqualTo("2"); // rank preserved from full ranking
        }
    }

    @Test
    void throwsNotFoundWhenConvocatoriaDoesNotBelongToTenant() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new ExportConvocatoriaRankingQuery(convId, tenantId, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private Convocatoria draftConvocatoria() {
        return Convocatoria.builder()
                .id(convId).tenantId(tenantId).name("Analista de RRHH")
                .status(ConvocatoriaStatus.ACTIVE)
                .build();
    }
}
