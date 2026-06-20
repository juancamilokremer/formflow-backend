package com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.ConvocatoriaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConvocatoriaJpaRepository extends JpaRepository<ConvocatoriaJpaEntity, UUID> {

    Optional<ConvocatoriaJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    List<ConvocatoriaJpaEntity> findByTenantIdAndDeletedAtIsNull(UUID tenantId);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);

    @Modifying
    @Query("UPDATE ConvocatoriaJpaEntity c SET c.deletedAt = :now WHERE c.id = :id")
    void softDeleteById(@Param("id") UUID id, @Param("now") Instant now);
}
