package com.kodelabs.formflow.modules.auth.domain.port.out;

import java.time.Instant;
import java.util.UUID;

/**
 * What the auth module needs from the forms module to compute tenant usage
 * (backend#5). Same cross-module pattern as forms' TenantInfoPort: the port
 * lives with the consumer, the adapter lives with the data owner.
 */
public interface TenantUsagePort {

    long countForms(UUID tenantId);

    long countResponsesThisMonth(UUID tenantId, Instant from, Instant to);

    long countAllResponsesThisMonth(Instant from, Instant to);

    long countConvocatorias(UUID tenantId);
}
