package com.kodelabs.formflow.modules.forms.domain.port.out;

import com.kodelabs.formflow.modules.forms.domain.model.FormSection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FormSectionRepositoryPort {

    FormSection save(FormSection section);

    void saveAll(List<FormSection> sections);

    Optional<FormSection> findByIdAndFormIdAndTenantId(UUID id, UUID formId, UUID tenantId);

    /** Returns active (non-deleted) sections ordered by position. */
    List<FormSection> findActiveByFormIdAndTenantId(UUID formId, UUID tenantId);

    int countActiveByFormId(UUID formId);
}
