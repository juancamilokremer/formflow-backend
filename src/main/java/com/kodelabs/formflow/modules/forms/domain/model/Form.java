package com.kodelabs.formflow.modules.forms.domain.model;

import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Form aggregate root. Pure domain POJO — no JPA dependencies.
 * Sections are loaded on demand; not present in summary queries.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Form {

    private UUID id;
    private UUID tenantId;
    private String name;
    private String description;
    private FormType type;

    @Builder.Default
    private FormStatus status = FormStatus.DRAFT;

    @Builder.Default
    private int version = 1;

    private Integer timeLimitSeconds;

    private UUID previousVersionId;
    private UUID rootFormId;

    @Builder.Default
    private List<FormSection> sections = new ArrayList<>();

    private UUID createdBy;
    private UUID updatedBy;
    private Instant deletedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void incrementVersion() {
        this.version++;
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    /** Structural edits are blocked for CANDIDATES/DIAGNOSTIC forms once published or archived. */
    public boolean isLocked() {
        return status != FormStatus.DRAFT
                && (type == FormType.CANDIDATES || type == FormType.DIAGNOSTIC);
    }

    public void assertEditable() {
        if (isLocked()) {
            throw new BusinessException("error.question.form_locked", HttpStatus.BAD_REQUEST);
        }
    }

    public void assertLockedForVersioning() {
        if (!isLocked()) {
            throw new BusinessException("error.form.version_source_not_locked", HttpStatus.BAD_REQUEST);
        }
    }
}
