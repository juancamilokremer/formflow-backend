package com.kodelabs.formflow.modules.auth.domain.port.in.result;

import java.util.List;

public record TenantPageResult(
        List<TenantResult> items,
        long totalElements,
        int totalPages,
        int page,
        int size
) {}
