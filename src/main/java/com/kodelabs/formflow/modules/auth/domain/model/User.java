package com.kodelabs.formflow.modules.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Platform user. Always belongs to a tenant.
 *
 * Pure domain POJO — no JPA/Hibernate dependencies.
 * References the tenant by id (not by object) to keep aggregates decoupled
 * and ease a future split into microservices.
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
}
