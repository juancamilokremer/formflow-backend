package com.kodelabs.formflow.modules.auth.domain.model;

/**
 * Subscription plan of a tenant. Limits per plan are enforced by use cases.
 */
public enum TenantPlan {
    FREE, STARTER, PRO, BUSINESS, ENTERPRISE
}
