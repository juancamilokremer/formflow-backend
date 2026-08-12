package com.kodelabs.formflow.modules.reports.application.usecase;

import com.kodelabs.formflow.modules.reports.application.service.export.ExportRowBuilder;
import com.kodelabs.formflow.modules.reports.application.service.export.ResponseExporter;
import com.kodelabs.formflow.modules.reports.application.service.export.ResponseExporterRegistry;
import com.kodelabs.formflow.modules.reports.domain.model.ExportFormat;
import com.kodelabs.formflow.modules.reports.domain.model.ExportableForm;
import com.kodelabs.formflow.modules.reports.domain.model.ExportableFormData;
import com.kodelabs.formflow.modules.reports.domain.port.in.command.ExportFormResponsesQuery;
import com.kodelabs.formflow.modules.reports.domain.port.in.result.ExportResult;
import com.kodelabs.formflow.modules.reports.domain.port.out.FormResponseDataPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportFormResponsesServiceTest {

    @Mock private FormResponseDataPort dataPort;
    @Mock private ExportRowBuilder rowBuilder;
    @Mock private ResponseExporterRegistry exporterRegistry;
    @Mock private ResponseExporter excelExporter;
    @InjectMocks private ExportFormResponsesService service;

    private UUID formId;
    private UUID tenantId;
    private ExportableFormData data;

    @BeforeEach
    void setUp() {
        formId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        data = new ExportableFormData(new ExportableForm("Encuesta", List.of()), List.of());
    }

    @Test
    void loadsDataBuildsRowsAndDelegatesToTheMatchingExporter() {
        List<List<String>> rows = List.of(List.of("Fecha de envío"));
        ExportResult expected = new ExportResult(new byte[]{1, 2}, "encuesta.xlsx", "application/vnd.ms-excel");

        when(dataPort.load(formId, tenantId)).thenReturn(data);
        when(rowBuilder.build(data)).thenReturn(rows);
        when(exporterRegistry.find(ExportFormat.EXCEL)).thenReturn(Optional.of(excelExporter));
        when(excelExporter.export("Encuesta", rows)).thenReturn(expected);

        ExportResult result = service.execute(new ExportFormResponsesQuery(formId, tenantId, ExportFormat.EXCEL));

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void throwsWhenNoExporterIsRegisteredForTheRequestedFormat() {
        when(dataPort.load(formId, tenantId)).thenReturn(data);
        when(rowBuilder.build(data)).thenReturn(List.of());
        when(exporterRegistry.find(ExportFormat.CSV)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new ExportFormResponsesQuery(formId, tenantId, ExportFormat.CSV)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }
}
