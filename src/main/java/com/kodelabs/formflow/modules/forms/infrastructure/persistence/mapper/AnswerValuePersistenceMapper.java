package com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.domain.model.AnswerValue;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.AnswerValueJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnswerValuePersistenceMapper {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public AnswerValue toDomain(AnswerValueJpaEntity entity) {
        Object value = objectMapper.readValue(entity.getValue(), Object.class);
        return AnswerValue.builder()
                .id(entity.getId())
                .responseId(entity.getResponseId())
                .questionId(entity.getQuestionId())
                .value(value)
                .build();
    }

    @SneakyThrows
    public AnswerValueJpaEntity toEntity(AnswerValue domain, java.util.UUID responseId) {
        return AnswerValueJpaEntity.builder()
                .id(domain.getId())
                .responseId(responseId)
                .questionId(domain.getQuestionId())
                .value(objectMapper.writeValueAsString(domain.getValue()))
                .build();
    }
}
