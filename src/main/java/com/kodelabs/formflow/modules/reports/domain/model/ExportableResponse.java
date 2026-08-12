package com.kodelabs.formflow.modules.reports.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * displayValuesByQuestionId already holds human-readable text (id->label
 * resolution happens in the adapter that builds this, not here) — a question
 * with no entry means the respondent left it unanswered.
 */
public record ExportableResponse(Instant submittedAt, Map<UUID, String> displayValuesByQuestionId) {}
