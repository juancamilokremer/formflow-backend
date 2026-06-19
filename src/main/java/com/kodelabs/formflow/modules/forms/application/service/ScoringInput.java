package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Input contract for ScoringEngine.
 *
 * @param questions      active questions belonging to the form being scored
 * @param obtainedScores map of questionId → score obtained by the respondent
 * @param categoryWeights map of categoryId → weight percentage (values must sum to 100)
 */
public record ScoringInput(
        List<FormQuestion> questions,
        Map<UUID, Integer> obtainedScores,
        Map<UUID, Double> categoryWeights
) {}
