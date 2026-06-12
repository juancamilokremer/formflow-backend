package com.kodelabs.formflow.modules.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Usuario de la plataforma. Siempre pertenece a un Tenant.
 *
 * POJO puro de dominio — sin dependencias de JPA/Hibernate.
 * Referencia al tenant por id (no por objeto) para mantener los agregados
 * desacoplados y facilitar la futura separación en microservicios.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private UUID id;

    private UUID tenantId;

    private String email;

    private String passwordHash;

    private String firstName;

    private String lastName;

    @Builder.Default
    private UserRole role = UserRole.VIEWER;

    @Builder.Default
    private boolean active = true;

    private Instant createdAt;

    private Instant updatedAt;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public enum UserRole {
        TENANT_ADMIN,  // Administrador de la empresa
        EDITOR,        // Puede crear y editar formularios
        VIEWER         // Solo puede ver resultados
    }
}
