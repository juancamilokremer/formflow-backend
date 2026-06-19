package com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.FormSectionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FormSectionJpaRepository extends JpaRepository<FormSectionJpaEntity, UUID> {

    @Query("SELECT s FROM FormSectionJpaEntity s WHERE s.id = :id AND s.formId = :formId AND s.tenantId = :tenantId AND s.deletedAt IS NULL")
    Optional<FormSectionJpaEntity> findActiveByIdAndFormIdAndTenantId(
            @Param("id") UUID id, @Param("formId") UUID formId, @Param("tenantId") UUID tenantId);

    @Query("SELECT s FROM FormSectionJpaEntity s WHERE s.formId = :formId AND s.tenantId = :tenantId AND s.deletedAt IS NULL ORDER BY s.position ASC")
    List<FormSectionJpaEntity> findActiveByFormIdAndTenantId(
            @Param("formId") UUID formId, @Param("tenantId") UUID tenantId);

    @Query("SELECT COUNT(s) FROM FormSectionJpaEntity s WHERE s.formId = :formId AND s.deletedAt IS NULL")
    int countActiveByFormId(@Param("formId") UUID formId);

    @Query("SELECT s.formId, COUNT(s) FROM FormSectionJpaEntity s WHERE s.formId IN :formIds AND s.deletedAt IS NULL GROUP BY s.formId")
    List<Object[]> countActiveGroupByFormId(@Param("formIds") List<UUID> formIds);

    @Query("SELECT s.id FROM FormSectionJpaEntity s WHERE s.formId = :formId AND s.deletedAt IS NULL")
    List<UUID> findActiveIdsByFormId(@Param("formId") UUID formId);
}
