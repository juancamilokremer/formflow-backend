package com.kodelabs.formflow.modules.reports.domain.port.in.command;

import com.kodelabs.formflow.modules.reports.domain.model.ExportFormat;

import java.util.UUID;

public record ExportFormResponsesQuery(UUID formId, UUID tenantId, ExportFormat format) {}
