package com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.application.service.QuestionTypeRegistry;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionalLogic;
import com.kodelabs.formflow.modules.forms.domain.model.config.QuestionConfig;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.FormQuestionJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FormQuestionPersistenceMapper {

    private final QuestionTypeRegistry registry;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public FormQuestion toDomain(FormQuestionJpaEntity entity) {
        QuestionType type = QuestionType.of(entity.getType());
        QuestionConfig config = registry.get(type).deserialize(
                entity.getConfig() == null || entity.getConfig().isBlank() ? "{}" : entity.getConfig());
        ConditionalLogic conditionalLogic = entity.getConditionalLogic() == null
                ? null
                : objectMapper.readValue(entity.getConditionalLogic(), ConditionalLogic.class);
        return FormQuestion.builder()
                .id(entity.getId())
                .sectionId(entity.getSectionId())
                .formId(entity.getFormId())
                .tenantId(entity.getTenantId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .type(type)
                .position(entity.getPosition())
                .required(entity.isRequired())
                .categoryId(entity.getCategoryId())
                .timeLimitSeconds(entity.getTimeLimitSeconds())
                .config(config)
                .conditionalLogic(conditionalLogic)
                .deletedAt(entity.getDeletedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @SneakyThrows
    public FormQuestionJpaEntity toEntity(FormQuestion domain) {
        return FormQuestionJpaEntity.builder()
                .id(domain.getId())
                .sectionId(domain.getSectionId())
                .formId(domain.getFormId())
                .tenantId(domain.getTenantId())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .type(domain.getType().code())
                .position(domain.getPosition())
                .required(domain.isRequired())
                .categoryId(domain.getCategoryId())
                .timeLimitSeconds(domain.getTimeLimitSeconds())
                .config(objectMapper.writeValueAsString(domain.getConfig()))
                .conditionalLogic(domain.getConditionalLogic() == null
                        ? null
                        : objectMapper.writeValueAsString(domain.getConditionalLogic()))
                .deletedAt(domain.getDeletedAt())
                .build();
    }
}
