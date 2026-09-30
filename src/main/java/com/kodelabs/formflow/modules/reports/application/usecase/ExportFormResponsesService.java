package com.kodelabs.formflow.modules.reports.application.usecase;

import com.kodelabs.formflow.modules.reports.application.service.export.ExportRowBuilder;
import com.kodelabs.formflow.modules.reports.application.service.export.ResponseExporter;
import com.kodelabs.formflow.modules.reports.application.service.export.ResponseExporterRegistry;
import com.kodelabs.formflow.modules.reports.domain.model.ExportableFormData;
import com.kodelabs.formflow.modules.reports.domain.port.in.ExportFormResponsesUseCase;
import com.kodelabs.formflow.modules.reports.domain.port.in.command.ExportFormResponsesQuery;
import com.kodelabs.formflow.modules.reports.domain.port.in.result.ExportResult;
import com.kodelabs.formflow.modules.reports.domain.model.ExportFormat;
import com.kodelabs.formflow.modules.reports.domain.port.out.FormResponseDataPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import com.kodelabs.formflow.shared.planlimit.PlanLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportFormResponsesService implements ExportFormResponsesUseCase {

    private final FormResponseDataPort dataPort;
    private final ExportRowBuilder rowBuilder;
    private final ResponseExporterRegistry exporterRegistry;
    private final PlanLimitService planLimitService;

    @Override
    public ExportResult execute(ExportFormResponsesQuery query) {
        if (query.format() == ExportFormat.EXCEL && !planLimitService.canExportExcel(query.tenantId())) {
            throw new BusinessException("error.plan_limit.export_excel", HttpStatus.PAYMENT_REQUIRED);
        }
        ExportableFormData data = dataPort.load(
                query.formId(), query.tenantId(), query.submittedAtFrom(), query.submittedAtTo());
        List<List<String>> rows = rowBuilder.build(data, query.timezone());
        ResponseExporter exporter = exporterRegistry.find(query.format())
                .orElseThrow(() -> new BusinessException(
                        "error.export.unsupported_format", HttpStatus.BAD_REQUEST, query.format()));
        return exporter.export(data.form().formName(), rows);
    }
}
