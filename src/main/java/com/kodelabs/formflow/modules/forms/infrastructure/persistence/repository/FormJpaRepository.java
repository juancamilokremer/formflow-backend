package com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.FormJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FormJpaRepository extends Repository<FormJpaEntity, UUID> {

    /** Flushed: @CreationTimestamp/@UpdateTimestamp only populate the entity when the INSERT
     *  runs, and callers map the result to a DTO right away. See #122. */
    FormJpaEntity saveAndFlush(FormJpaEntity form);

    @Query("SELECT f FROM FormJpaEntity f WHERE f.id = :id AND f.tenantId = :tenantId AND f.deletedAt IS NULL")
    Optional<FormJpaEntity> findActiveByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    /** Locks the row for the rest of the transaction. Used before computing a position from a
     *  count()/reading version to increment it, so two concurrent structural changes to the
     *  same form serialize instead of racing (duplicate positions, lost version increments). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM FormJpaEntity f WHERE f.id = :id AND f.tenantId = :tenantId AND f.deletedAt IS NULL")
    Optional<FormJpaEntity> findActiveByIdAndTenantIdForUpdate(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query("SELECT f FROM FormJpaEntity f WHERE f.tenantId = :tenantId AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    List<FormJpaEntity> findAllActiveByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT COUNT(f) > 0 FROM FormJpaEntity f WHERE f.id = :id AND f.tenantId = :tenantId AND f.deletedAt IS NULL")
    boolean existsActiveByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query("SELECT f FROM FormJpaEntity f WHERE f.id = :id AND f.deletedAt IS NULL")
    Optional<FormJpaEntity> findActiveById(@Param("id") UUID id);

    @Query("SELECT MAX(f.version) FROM FormJpaEntity f WHERE f.tenantId = :tenantId AND f.deletedAt IS NULL " +
            "AND (f.id = :rootId OR f.rootFormId = :rootId)")
    Integer findMaxVersionInFamily(@Param("rootId") UUID rootId, @Param("tenantId") UUID tenantId);

    @Query("SELECT f FROM FormJpaEntity f WHERE f.tenantId = :tenantId AND f.deletedAt IS NULL " +
            "AND (f.id = :rootId OR f.rootFormId = :rootId) ORDER BY f.version ASC")
    List<FormJpaEntity> findFamilyByRootId(@Param("rootId") UUID rootId, @Param("tenantId") UUID tenantId);
}
