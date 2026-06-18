package com.kodelabs.formflow.modules.forms.domain.port.out;

import com.kodelabs.formflow.modules.forms.domain.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepositoryPort {

    Category save(Category category);

    Optional<Category> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Category> findAllByTenantId(UUID tenantId);

    /** Returns categories by their IDs, filtered to the given tenant. */
    List<Category> findAllByIdsAndTenantId(List<UUID> ids, UUID tenantId);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByNameAndTenantId(String name, UUID tenantId);

    void deleteById(UUID id);
}
