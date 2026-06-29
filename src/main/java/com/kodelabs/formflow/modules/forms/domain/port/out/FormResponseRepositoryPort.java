package com.kodelabs.formflow.modules.forms.domain.port.out;

import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface FormResponseRepositoryPort {

    FormResponse save(FormResponse response);

    Optional<FormResponse> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<FormResponse> findByRespondentToken(UUID respondentToken);

    boolean existsByRespondentToken(UUID respondentToken);

    List<FormResponse> findAllByFormIdAndTenantId(UUID formId, UUID tenantId);

    Map<UUID, Integer> countByFormIds(List<UUID> formIds);

    Map<UUID, Instant> lastResponseAtByFormIds(List<UUID> formIds);
}
