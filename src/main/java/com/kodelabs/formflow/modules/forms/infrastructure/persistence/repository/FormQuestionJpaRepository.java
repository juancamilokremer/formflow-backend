package com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.FormQuestionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FormQuestionJpaRepository extends JpaRepository<FormQuestionJpaEntity, UUID> {

    @Query("SELECT q FROM FormQuestionJpaEntity q WHERE q.id = :id AND q.sectionId = :sectionId AND q.tenantId = :tenantId AND q.deletedAt IS NULL")
    Optional<FormQuestionJpaEntity> findActiveByIdAndSectionIdAndTenantId(
            @Param("id") UUID id, @Param("sectionId") UUID sectionId, @Param("tenantId") UUID tenantId);

    @Query("SELECT q FROM FormQuestionJpaEntity q WHERE q.sectionId = :sectionId AND q.tenantId = :tenantId AND q.deletedAt IS NULL ORDER BY q.position ASC")
    List<FormQuestionJpaEntity> findActiveBySectionIdAndTenantId(
            @Param("sectionId") UUID sectionId, @Param("tenantId") UUID tenantId);

    @Query("SELECT COUNT(q) FROM FormQuestionJpaEntity q WHERE q.sectionId = :sectionId AND q.deletedAt IS NULL")
    int countActiveBySectionId(@Param("sectionId") UUID sectionId);

    @Query("SELECT q FROM FormQuestionJpaEntity q WHERE q.sectionId IN :sectionIds AND q.deletedAt IS NULL ORDER BY q.position ASC")
    List<FormQuestionJpaEntity> findActiveBySectionIdIn(@Param("sectionIds") List<UUID> sectionIds);
}
