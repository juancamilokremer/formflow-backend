package com.kodelabs.formflow.modules.auth.domain.model;

/**
 * Role of a user within its tenant.
 */
public enum UserRole {
    /** Company administrator: full access within the tenant. */
    TENANT_ADMIN,
    /** Can create and edit forms. */
    EDITOR,
    /** Read-only access to results. */
    VIEWER
}
