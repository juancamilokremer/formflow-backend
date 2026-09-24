package com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.ConvocatoriaFormJpaEntity;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface ConvocatoriaFormJpaRepository extends Repository<ConvocatoriaFormJpaEntity, UUID> {

    /** Flushed: @CreationTimestamp/@UpdateTimestamp only populate the entity when the
     *  INSERT runs, and callers map the result to a DTO right away. See #122. */
    ConvocatoriaFormJpaEntity saveAndFlush(ConvocatoriaFormJpaEntity form);

    List<ConvocatoriaFormJpaEntity> saveAll(Iterable<ConvocatoriaFormJpaEntity> forms);

    void deleteById(UUID id);

    List<ConvocatoriaFormJpaEntity> findAllByConvocatoriaIdOrderByPositionAsc(UUID convocatoriaId);

    List<ConvocatoriaFormJpaEntity> findAllByFormId(UUID formId);

    List<ConvocatoriaFormJpaEntity> findAllByConvocatoriaIdInOrderByPositionAsc(List<UUID> convocatoriaIds);

    int countByConvocatoriaId(UUID convocatoriaId);
}
