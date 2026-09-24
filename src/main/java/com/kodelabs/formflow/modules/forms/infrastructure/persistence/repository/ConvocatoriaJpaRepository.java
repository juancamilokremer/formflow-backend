package com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.ConvocatoriaJpaEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConvocatoriaJpaRepository extends Repository<ConvocatoriaJpaEntity, UUID> {

    /** Flushed: @CreationTimestamp/@UpdateTimestamp only populate the entity when the
     *  INSERT runs, and callers map the result to a DTO right away. See #122. */
    ConvocatoriaJpaEntity saveAndFlush(ConvocatoriaJpaEntity convocatoria);

    Optional<ConvocatoriaJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    List<ConvocatoriaJpaEntity> findByTenantIdAndDeletedAtIsNull(UUID tenantId);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);

    @Modifying
    @Query("UPDATE ConvocatoriaJpaEntity c SET c.deletedAt = :now WHERE c.id = :id")
    void softDeleteById(@Param("id") UUID id, @Param("now") Instant now);
}
