package com.kodelabs.formflow.modules.reports.domain.port.out;

import com.kodelabs.formflow.modules.reports.domain.model.ExportableFormData;

import java.time.Instant;
import java.util.UUID;

/**
 * Anti-corruption layer boundary towards the forms module — owned by reports,
 * implemented from forms' infrastructure layer (same pattern as TenantInfoPort).
 */
public interface FormResponseDataPort {

    ExportableFormData load(UUID formId, UUID tenantId, Instant submittedAtFrom, Instant submittedAtTo);
}
