package com.kodelabs.formflow.modules.auth.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa una empresa cliente (tenant) en la plataforma.
 * Cada tenant tiene su propio espacio de datos aislado.
 */
@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String slug; // identificador único URL-friendly: "empresa-abc"

    @Column(nullable = false, length = 150)
    private String name; // nombre visible: "Empresa ABC S.A.S"

    @Column(length = 200)
    private String logoUrl;

    @Column(length = 7)
    private String primaryColor; // hex: "#3B82F6"

    @Column(length = 7)
    private String secondaryColor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TenantPlan plan = TenantPlan.FREE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TenantStatus status = TenantStatus.ACTIVE;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public enum TenantPlan {
        FREE, STARTER, PRO, BUSINESS, ENTERPRISE
    }

    public enum TenantStatus {
        ACTIVE, SUSPENDED, CANCELLED
    }
}
