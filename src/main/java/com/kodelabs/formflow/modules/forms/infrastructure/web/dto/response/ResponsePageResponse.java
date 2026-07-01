package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.ResponsePageResult;

import java.util.List;

public record ResponsePageResponse(
        List<ResponseSummaryResponse> items,
        long totalElements,
        int totalPages,
        int page,
        int size
) {
    public static ResponsePageResponse from(ResponsePageResult r) {
        return new ResponsePageResponse(
                r.items().stream().map(ResponseSummaryResponse::from).toList(),
                r.totalElements(), r.totalPages(), r.page(), r.size());
    }
}
