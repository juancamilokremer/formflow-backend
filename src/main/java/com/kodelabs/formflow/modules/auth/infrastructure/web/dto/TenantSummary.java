package com.kodelabs.formflow.modules.auth.infrastructure.web.dto;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;

import java.util.UUID;

/**
 * Basic tenant data returned by the authentication endpoints.
 */
public record TenantSummary(UUID id, String slug, String name, String plan) {

    public static TenantSummary from(Tenant tenant) {
        return new TenantSummary(tenant.getId(), tenant.getSlug(), tenant.getName(), tenant.getPlan().name());
    }
}
