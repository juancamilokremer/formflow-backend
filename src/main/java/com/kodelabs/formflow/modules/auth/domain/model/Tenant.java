package com.kodelabs.formflow.modules.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa una empresa cliente (tenant) en la plataforma.
 * Cada tenant tiene su propio espacio de datos aislado.
 *
 * POJO puro de dominio — sin dependencias de JPA/Hibernate.
 * Su representación en base de datos es TenantJpaEntity (infrastructure/persistence).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    private UUID id;

    private String slug; // identificador único URL-friendly: "empresa-abc"

    private String name; // nombre visible: "Empresa ABC S.A.S"

    private String logoUrl;

    private String primaryColor; // hex: "#3B82F6"

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

    public enum TenantPlan {
        FREE, STARTER, PRO, BUSINESS, ENTERPRISE
    }

    public enum TenantStatus {
        ACTIVE, SUSPENDED, CANCELLED
    }
}
