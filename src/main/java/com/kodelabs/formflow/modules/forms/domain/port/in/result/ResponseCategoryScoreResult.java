package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import java.util.UUID;

public record ResponseCategoryScoreResult(UUID categoryId, String categoryName, double score) {}
