package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.time.Instant;
import java.util.UUID;

public record GetFormStatsQuery(UUID formId, UUID tenantId, Instant submittedAtFrom, Instant submittedAtTo) {}
