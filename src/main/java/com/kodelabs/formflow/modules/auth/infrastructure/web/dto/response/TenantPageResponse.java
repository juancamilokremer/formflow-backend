package com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantPageResult;

import java.util.List;

public record TenantPageResponse(
        List<TenantResponse> items,
        long totalElements,
        int totalPages,
        int page,
        int size
) {
    public static TenantPageResponse from(TenantPageResult r) {
        return new TenantPageResponse(
                r.items().stream().map(TenantResponse::from).toList(),
                r.totalElements(), r.totalPages(), r.page(), r.size());
    }
}
