package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import java.util.List;

public record ResponsePageResult(
        List<ResponseSummaryResult> items,
        long totalElements,
        int totalPages,
        int page,
        int size
) {}
