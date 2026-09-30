package com.kodelabs.formflow.modules.auth.domain.port.in.result;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.model.TenantStatus;

import java.time.Instant;
import java.util.UUID;

public record TenantResult(
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
    public static TenantResult from(Tenant t) {
        return new TenantResult(
                t.getId(), t.getSlug(), t.getName(), t.getLogoUrl(),
                t.getPrimaryColor(), t.getSecondaryColor(), t.getPlan(), t.getStatus(),
                t.getCreatedAt(), t.getUpdatedAt());
    }
}
