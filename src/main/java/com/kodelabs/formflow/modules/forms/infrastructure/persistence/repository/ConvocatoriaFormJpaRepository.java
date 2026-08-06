package com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.ConvocatoriaFormJpaEntity;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface ConvocatoriaFormJpaRepository extends Repository<ConvocatoriaFormJpaEntity, UUID> {

    ConvocatoriaFormJpaEntity save(ConvocatoriaFormJpaEntity form);

    List<ConvocatoriaFormJpaEntity> saveAll(Iterable<ConvocatoriaFormJpaEntity> forms);

    void deleteById(UUID id);

    List<ConvocatoriaFormJpaEntity> findAllByConvocatoriaIdOrderByPositionAsc(UUID convocatoriaId);

    List<ConvocatoriaFormJpaEntity> findAllByConvocatoriaIdInOrderByPositionAsc(List<UUID> convocatoriaIds);

    int countByConvocatoriaId(UUID convocatoriaId);
}
