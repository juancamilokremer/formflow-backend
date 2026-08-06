package com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryJpaRepository extends Repository<CategoryJpaEntity, UUID> {

    CategoryJpaEntity save(CategoryJpaEntity category);

    void deleteById(UUID id);

    Optional<CategoryJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    List<CategoryJpaEntity> findAllByTenantIdOrderByNameAsc(UUID tenantId);

    @Query("SELECT c FROM CategoryJpaEntity c WHERE c.id IN :ids AND c.tenantId = :tenantId")
    List<CategoryJpaEntity> findAllByIdsAndTenantId(
            @Param("ids") List<UUID> ids, @Param("tenantId") UUID tenantId);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByNameAndTenantId(String name, UUID tenantId);
}
