package com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.FormResponseJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FormResponseJpaRepository extends Repository<FormResponseJpaEntity, UUID> {

    FormResponseJpaEntity save(FormResponseJpaEntity response);

    Optional<FormResponseJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<FormResponseJpaEntity> findByRespondentToken(UUID respondentToken);

    boolean existsByRespondentToken(UUID respondentToken);

    boolean existsByCandidateIdAndFormId(UUID candidateId, UUID formId);

    long countByCandidateId(UUID candidateId);

    @Query("SELECT r FROM FormResponseJpaEntity r WHERE r.formId = :formId AND r.tenantId = :tenantId "
            + "AND r.submittedAt >= COALESCE(:submittedAtFrom, r.submittedAt) "
            + "AND r.submittedAt <= COALESCE(:submittedAtTo, r.submittedAt)")
    List<FormResponseJpaEntity> findAllByFormIdAndTenantId(
            @Param("formId") UUID formId, @Param("tenantId") UUID tenantId,
            @Param("submittedAtFrom") Instant submittedAtFrom, @Param("submittedAtTo") Instant submittedAtTo);

    @Query("SELECT r FROM FormResponseJpaEntity r WHERE r.formId = :formId AND r.tenantId = :tenantId "
            + "AND r.submittedAt >= COALESCE(:submittedAtFrom, r.submittedAt) "
            + "AND r.submittedAt <= COALESCE(:submittedAtTo, r.submittedAt) "
            + "ORDER BY r.submittedAt DESC")
    Page<FormResponseJpaEntity> findPageByFormAndTenant(
            @Param("formId") UUID formId, @Param("tenantId") UUID tenantId,
            @Param("submittedAtFrom") Instant submittedAtFrom, @Param("submittedAtTo") Instant submittedAtTo,
            Pageable pageable);

    @Query("SELECT COUNT(r) FROM FormResponseJpaEntity r WHERE r.formId = :formId AND r.tenantId = :tenantId "
            + "AND r.submittedAt >= COALESCE(:submittedAtFrom, r.submittedAt) "
            + "AND r.submittedAt <= COALESCE(:submittedAtTo, r.submittedAt)")
    long countByFormIdAndTenantId(
            @Param("formId") UUID formId, @Param("tenantId") UUID tenantId,
            @Param("submittedAtFrom") Instant submittedAtFrom, @Param("submittedAtTo") Instant submittedAtTo);

    @Query("SELECT r.formId, COUNT(r.id) FROM FormResponseJpaEntity r WHERE r.formId IN :formIds GROUP BY r.formId")
    List<Object[]> countGroupedByFormIds(@Param("formIds") List<UUID> formIds);

    @Query("SELECT r.formId, MAX(r.createdAt) FROM FormResponseJpaEntity r WHERE r.formId IN :formIds GROUP BY r.formId")
    List<Object[]> lastCreatedAtGroupedByFormIds(@Param("formIds") List<UUID> formIds);
}
