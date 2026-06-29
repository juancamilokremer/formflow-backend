package com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.AnswerValueJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnswerValueJpaRepository extends JpaRepository<AnswerValueJpaEntity, UUID> {

    List<AnswerValueJpaEntity> findAllByResponseId(UUID responseId);

    void deleteAllByResponseId(UUID responseId);
}
