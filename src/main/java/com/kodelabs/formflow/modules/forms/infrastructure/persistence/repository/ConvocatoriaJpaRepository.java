package com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.ConvocatoriaJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
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

    /** Locks the row for the rest of the transaction. Used before computing a position from a
     *  count() (CreateConvocatoriaFormService), so two concurrent form-attach requests on the
     *  same convocatoria serialize instead of racing on duplicate positions (see backend#162). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ConvocatoriaJpaEntity c WHERE c.id = :id AND c.tenantId = :tenantId")
    Optional<ConvocatoriaJpaEntity> findByIdAndTenantIdForUpdate(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    List<ConvocatoriaJpaEntity> findByTenantIdAndDeletedAtIsNull(UUID tenantId);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);

    @Modifying
    @Query("UPDATE ConvocatoriaJpaEntity c SET c.deletedAt = :now WHERE c.id = :id")
    void softDeleteById(@Param("id") UUID id, @Param("now") Instant now);
}
