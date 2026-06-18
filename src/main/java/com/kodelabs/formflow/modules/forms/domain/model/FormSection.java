package com.kodelabs.formflow.modules.forms.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A section within a Form. Pure domain POJO — no JPA dependencies.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormSection {

    private UUID id;
    private UUID formId;
    private UUID tenantId;
    private String title;
    private String description;
    private int position;
    private Integer timeLimitSeconds;
    @Builder.Default
    private List<FormQuestion> questions = new ArrayList<>();
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
