package com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.domain.model.AnswerValue;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.FormSnapshot;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.AnswerValueJpaEntity;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.FormResponseJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FormResponsePersistenceMapper {

    private final ObjectMapper objectMapper;
    private final AnswerValuePersistenceMapper answerMapper;

    @SneakyThrows
    public FormResponse toDomain(FormResponseJpaEntity entity, List<AnswerValueJpaEntity> answerEntities) {
        FormSnapshot snapshot = objectMapper.readValue(entity.getFormSnapshot(), FormSnapshot.class);
        List<AnswerValue> answers = answerEntities.stream().map(answerMapper::toDomain).toList();
        return FormResponse.builder()
                .id(entity.getId())
                .formId(entity.getFormId())
                .tenantId(entity.getTenantId())
                .convocatoriaId(entity.getConvocatoriaId())
                .candidateId(entity.getCandidateId())
                .respondentToken(entity.getRespondentToken())
                .formSnapshot(snapshot)
                .answers(answers)
                .startedAt(entity.getStartedAt())
                .submittedAt(entity.getSubmittedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @SneakyThrows
    public FormResponseJpaEntity toEntity(FormResponse domain) {
        return FormResponseJpaEntity.builder()
                .id(domain.getId())
                .formId(domain.getFormId())
                .tenantId(domain.getTenantId())
                .convocatoriaId(domain.getConvocatoriaId())
                .candidateId(domain.getCandidateId())
                .respondentToken(domain.getRespondentToken())
                .formSnapshot(objectMapper.writeValueAsString(domain.getFormSnapshot()))
                .startedAt(domain.getStartedAt())
                .submittedAt(domain.getSubmittedAt() != null ? domain.getSubmittedAt() : Instant.now())
                .build();
    }
}
