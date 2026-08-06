package com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.AnswerValueJpaEntity;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface AnswerValueJpaRepository extends Repository<AnswerValueJpaEntity, UUID> {

    List<AnswerValueJpaEntity> saveAll(Iterable<AnswerValueJpaEntity> answers);

    List<AnswerValueJpaEntity> findAllByResponseId(UUID responseId);

    void deleteAllByResponseId(UUID responseId);
}
