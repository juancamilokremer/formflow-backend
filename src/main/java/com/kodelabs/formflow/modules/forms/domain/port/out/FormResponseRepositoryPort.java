package com.kodelabs.formflow.modules.forms.domain.port.out;

import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;

import java.util.Optional;
import java.util.UUID;

public interface FormResponseRepositoryPort {

    FormResponse save(FormResponse response);

    Optional<FormResponse> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<FormResponse> findByRespondentToken(UUID respondentToken);

    boolean existsByRespondentToken(UUID respondentToken);
}
