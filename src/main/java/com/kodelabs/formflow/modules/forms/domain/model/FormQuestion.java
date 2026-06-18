package com.kodelabs.formflow.modules.forms.domain.model;

import com.kodelabs.formflow.modules.forms.domain.model.config.QuestionConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * A question within a FormSection. Pure domain POJO — no JPA dependencies.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormQuestion {

    private UUID id;
    private UUID sectionId;
    private UUID formId;
    private UUID tenantId;
    private String title;
    private String description;
    private QuestionType type;
    private int position;
    @Builder.Default
    private boolean required = false;
    private UUID categoryId;
    private Integer timeLimitSeconds;
    private QuestionConfig config;
    private Instant deletedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }
}
