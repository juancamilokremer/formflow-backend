package com.kodelabs.formflow.modules.forms.application.service;

/**
 * Score breakdown for a single category within a scoring calculation.
 *
 * @param obtained     raw points the respondent obtained in this category
 * @param maxPossible  maximum points achievable in this category
 * @param weight       category weight as a percentage (0–100)
 * @param contribution final contribution to the total score: (obtained / maxPossible) * weight
 */
public record CategoryScore(
        int obtained,
        int maxPossible,
        double weight,
        double contribution
) {}
