package com.kodelabs.formflow.modules.forms.application.service;

import java.util.Map;
import java.util.UUID;

/**
 * Result of a scoring calculation.
 *
 * @param totalScore        final score out of 100
 * @param scoresByCategory  per-category breakdown, keyed by categoryId
 */
public record ScoringResult(
        double totalScore,
        Map<UUID, CategoryScore> scoresByCategory
) {}
