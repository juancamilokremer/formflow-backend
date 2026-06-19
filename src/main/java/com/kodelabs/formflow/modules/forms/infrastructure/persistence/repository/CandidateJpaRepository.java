package com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.CandidateJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CandidateJpaRepository extends JpaRepository<CandidateJpaEntity, UUID> {

    Optional<CandidateJpaEntity> findByIdAndConvocatoriaId(UUID id, UUID convocatoriaId);

    Optional<CandidateJpaEntity> findByToken(UUID token);

    List<CandidateJpaEntity> findAllByConvocatoriaId(UUID convocatoriaId);

    boolean existsByConvocatoriaIdAndEmail(UUID convocatoriaId, String email);

    long countByConvocatoriaId(UUID convocatoriaId);
}
