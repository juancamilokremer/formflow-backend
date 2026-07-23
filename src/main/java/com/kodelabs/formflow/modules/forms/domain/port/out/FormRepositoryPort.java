package com.kodelabs.formflow.modules.forms.domain.port.out;

import com.kodelabs.formflow.modules.forms.domain.model.Form;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FormRepositoryPort {

    Form save(Form form);

    /** Returns the form without sections (summary use). */
    Optional<Form> findByIdAndTenantId(UUID id, UUID tenantId);

    /** Returns the form with its active sections populated (detail use). */
    Optional<Form> findByIdAndTenantIdWithSections(UUID id, UUID tenantId);

    /** Returns all non-deleted forms for the tenant, without sections. */
    List<Form> findAllByTenantId(UUID tenantId);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);

    /** Returns a non-deleted form with sections and questions loaded, without tenant constraint. */
    Optional<Form> findByIdPublicWithSections(UUID formId);

    /** Highest version number among all forms sharing the given lineage root (root included). */
    int findMaxVersionInFamily(UUID rootId, UUID tenantId);

    /** All forms sharing the given lineage root (root included), ordered by version ascending. */
    List<Form> findFamilyByRootId(UUID rootId, UUID tenantId);
}
