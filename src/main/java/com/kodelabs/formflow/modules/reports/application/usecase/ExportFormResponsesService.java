package com.kodelabs.formflow.modules.reports.application.usecase;

import com.kodelabs.formflow.modules.reports.application.service.export.ExportRowBuilder;
import com.kodelabs.formflow.modules.reports.application.service.export.ResponseExporter;
import com.kodelabs.formflow.modules.reports.application.service.export.ResponseExporterRegistry;
import com.kodelabs.formflow.modules.reports.domain.model.ExportableFormData;
import com.kodelabs.formflow.modules.reports.domain.port.in.ExportFormResponsesUseCase;
import com.kodelabs.formflow.modules.reports.domain.port.in.command.ExportFormResponsesQuery;
import com.kodelabs.formflow.modules.reports.domain.port.in.result.ExportResult;
import com.kodelabs.formflow.modules.reports.domain.port.out.FormResponseDataPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
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

    @Override
    public ExportResult execute(ExportFormResponsesQuery query) {
        ExportableFormData data = dataPort.load(
                query.formId(), query.tenantId(), query.submittedAtFrom(), query.submittedAtTo());
        List<List<String>> rows = rowBuilder.build(data);
        ResponseExporter exporter = exporterRegistry.find(query.format())
                .orElseThrow(() -> new BusinessException(
                        "error.export.unsupported_format", HttpStatus.BAD_REQUEST, query.format()));
        return exporter.export(data.form().formName(), rows);
    }
}
