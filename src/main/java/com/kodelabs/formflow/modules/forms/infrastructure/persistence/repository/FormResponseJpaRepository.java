package com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.FormResponseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FormResponseJpaRepository extends JpaRepository<FormResponseJpaEntity, UUID> {

    Optional<FormResponseJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<FormResponseJpaEntity> findByRespondentToken(UUID respondentToken);

    boolean existsByRespondentToken(UUID respondentToken);
}
