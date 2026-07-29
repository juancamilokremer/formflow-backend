package com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.ConvocatoriaFormJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConvocatoriaFormJpaRepository extends JpaRepository<ConvocatoriaFormJpaEntity, UUID> {

    List<ConvocatoriaFormJpaEntity> findAllByConvocatoriaIdOrderByPositionAsc(UUID convocatoriaId);

    List<ConvocatoriaFormJpaEntity> findAllByConvocatoriaIdInOrderByPositionAsc(List<UUID> convocatoriaIds);

    int countByConvocatoriaId(UUID convocatoriaId);
}
