package com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.FormJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FormJpaRepository extends JpaRepository<FormJpaEntity, UUID> {

    @Query("SELECT f FROM FormJpaEntity f WHERE f.id = :id AND f.tenantId = :tenantId AND f.deletedAt IS NULL")
    Optional<FormJpaEntity> findActiveByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query("SELECT f FROM FormJpaEntity f WHERE f.tenantId = :tenantId AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    List<FormJpaEntity> findAllActiveByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT COUNT(f) > 0 FROM FormJpaEntity f WHERE f.id = :id AND f.tenantId = :tenantId AND f.deletedAt IS NULL")
    boolean existsActiveByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query("SELECT f FROM FormJpaEntity f WHERE f.id = :id AND f.deletedAt IS NULL")
    Optional<FormJpaEntity> findActiveById(@Param("id") UUID id);

    @Query("SELECT MAX(f.version) FROM FormJpaEntity f WHERE f.tenantId = :tenantId AND f.deletedAt IS NULL " +
            "AND (f.id = :rootId OR f.rootFormId = :rootId)")
    Integer findMaxVersionInFamily(@Param("rootId") UUID rootId, @Param("tenantId") UUID tenantId);
}
