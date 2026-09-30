package com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.auth.domain.port.in.result.BrandingResult;

public record BrandingResponse(
        String tenantName,
        String logoUrl,
        String primaryColor,
        String secondaryColor,
        String faviconUrl
) {
    public static BrandingResponse from(BrandingResult r) {
        return new BrandingResponse(r.tenantName(), r.logoUrl(), r.primaryColor(), r.secondaryColor(), r.faviconUrl());
    }
}
