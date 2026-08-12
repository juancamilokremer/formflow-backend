package com.kodelabs.formflow.modules.reports.application.service.export;

import com.kodelabs.formflow.modules.reports.domain.model.ExportFormat;
import com.kodelabs.formflow.modules.reports.domain.port.in.result.ExportResult;

import java.util.List;

public interface ResponseExporter {

    ExportFormat format();

    ExportResult export(String formName, List<List<String>> rows);
}
