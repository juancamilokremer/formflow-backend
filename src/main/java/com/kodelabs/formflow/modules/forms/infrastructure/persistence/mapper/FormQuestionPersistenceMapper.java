package com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.DateConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.FileConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.MatrixConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.MultipleConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.NpsConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.QuestionConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.ScaleConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.SingleConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.TextConfig;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.FormQuestionJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FormQuestionPersistenceMapper {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public FormQuestion toDomain(FormQuestionJpaEntity entity) {
        QuestionType type = QuestionType.valueOf(entity.getType());
        QuestionConfig config = deserializeConfig(type, entity.getConfig());
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
                .type(domain.getType().name())
                .position(domain.getPosition())
                .required(domain.isRequired())
                .categoryId(domain.getCategoryId())
                .timeLimitSeconds(domain.getTimeLimitSeconds())
                .config(objectMapper.writeValueAsString(domain.getConfig()))
                .deletedAt(domain.getDeletedAt())
                .build();
    }

    @SneakyThrows
    private QuestionConfig deserializeConfig(QuestionType type, String json) {
        if (json == null || json.isBlank()) json = "{}";
        return switch (type) {
            case TEXT -> objectMapper.readValue(json, TextConfig.class);
            case SINGLE -> objectMapper.readValue(json, SingleConfig.class);
            case MULTIPLE -> objectMapper.readValue(json, MultipleConfig.class);
            case SCALE -> objectMapper.readValue(json, ScaleConfig.class);
            case DATE -> objectMapper.readValue(json, DateConfig.class);
            case FILE -> objectMapper.readValue(json, FileConfig.class);
            case MATRIX -> objectMapper.readValue(json, MatrixConfig.class);
            case NPS -> objectMapper.readValue(json, NpsConfig.class);
        };
    }
}
