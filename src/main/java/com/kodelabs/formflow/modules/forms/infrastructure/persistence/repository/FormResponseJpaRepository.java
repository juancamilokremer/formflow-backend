package com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.FormResponseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FormResponseJpaRepository extends JpaRepository<FormResponseJpaEntity, UUID> {

    Optional<FormResponseJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<FormResponseJpaEntity> findByRespondentToken(UUID respondentToken);

    boolean existsByRespondentToken(UUID respondentToken);

    @Query("SELECT r.formId, COUNT(r.id) FROM FormResponseJpaEntity r WHERE r.formId IN :formIds GROUP BY r.formId")
    List<Object[]> countGroupedByFormIds(@Param("formIds") List<UUID> formIds);

    @Query("SELECT r.formId, MAX(r.createdAt) FROM FormResponseJpaEntity r WHERE r.formId IN :formIds GROUP BY r.formId")
    List<Object[]> lastCreatedAtGroupedByFormIds(@Param("formIds") List<UUID> formIds);
}
