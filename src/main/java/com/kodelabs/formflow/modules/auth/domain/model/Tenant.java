package com.kodelabs.formflow.modules.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * A client company (tenant) in the platform. Each tenant has its own
 * isolated data space.
 *
 * Pure domain POJO — no JPA/Hibernate dependencies.
 * Its database representation is TenantJpaEntity (infrastructure/persistence).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    private UUID id;

    /** Unique URL-friendly identifier, e.g. "empresa-abc". */
    private String slug;

    /** Display name, e.g. "Empresa ABC S.A.S". */
    private String name;

    private String logoUrl;

    /** Hex color, e.g. "#3B82F6". */
    private String primaryColor;

    private String secondaryColor;

    @Builder.Default
    private TenantPlan plan = TenantPlan.FREE;

    @Builder.Default
    private TenantStatus status = TenantStatus.ACTIVE;

    private Instant createdAt;

    private Instant updatedAt;

    public boolean isActive() {
        return status == TenantStatus.ACTIVE;
    }
}
