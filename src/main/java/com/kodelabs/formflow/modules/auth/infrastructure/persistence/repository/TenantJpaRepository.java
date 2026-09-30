package com.kodelabs.formflow.modules.auth.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.model.TenantStatus;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.entity.TenantJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantJpaRepository extends Repository<TenantJpaEntity, UUID> {

    /** Flushed: @CreationTimestamp/@UpdateTimestamp only populate the entity when the
     *  INSERT runs, and callers map the result to a DTO right away. See #122. */
    TenantJpaEntity saveAndFlush(TenantJpaEntity tenant);

    Optional<TenantJpaEntity> findById(UUID id);

    Optional<TenantJpaEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("SELECT t FROM TenantJpaEntity t WHERE "
            + "(:status IS NULL OR t.status = :status) AND (:plan IS NULL OR t.plan = :plan) "
            + "ORDER BY t.createdAt DESC")
    Page<TenantJpaEntity> findAll(
            @Param("status") TenantStatus status, @Param("plan") TenantPlan plan, Pageable pageable);

    @Query("SELECT COUNT(t) FROM TenantJpaEntity t WHERE "
            + "(:status IS NULL OR t.status = :status) AND (:plan IS NULL OR t.plan = :plan)")
    long countAll(@Param("status") TenantStatus status, @Param("plan") TenantPlan plan);

    @Query("SELECT t.plan, COUNT(t) FROM TenantJpaEntity t GROUP BY t.plan")
    List<Object[]> countGroupedByPlan();
}
