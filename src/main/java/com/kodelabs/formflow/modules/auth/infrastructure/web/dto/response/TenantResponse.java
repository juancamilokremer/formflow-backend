package com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.model.TenantStatus;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantResult;

import java.time.Instant;
import java.util.UUID;

public record TenantResponse(
        UUID id,
        String slug,
        String name,
        String logoUrl,
        String primaryColor,
        String secondaryColor,
        TenantPlan plan,
        TenantStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static TenantResponse from(TenantResult r) {
        return new TenantResponse(
                r.id(), r.slug(), r.name(), r.logoUrl(), r.primaryColor(), r.secondaryColor(),
                r.plan(), r.status(), r.createdAt(), r.updatedAt());
    }
}
