package com.kodelabs.formflow.modules.reports.domain.port.in;

import com.kodelabs.formflow.modules.reports.domain.port.in.command.ExportFormResponsesQuery;
import com.kodelabs.formflow.modules.reports.domain.port.in.result.ExportResult;

public interface ExportFormResponsesUseCase {

    ExportResult execute(ExportFormResponsesQuery query);
}
