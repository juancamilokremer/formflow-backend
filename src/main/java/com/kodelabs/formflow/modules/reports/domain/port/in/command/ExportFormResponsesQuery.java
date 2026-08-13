package com.kodelabs.formflow.modules.reports.domain.port.in.command;

import com.kodelabs.formflow.modules.reports.domain.model.ExportFormat;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

public record ExportFormResponsesQuery(
        UUID formId, UUID tenantId, ExportFormat format, Instant submittedAtFrom, Instant submittedAtTo,
        ZoneId timezone) {}
