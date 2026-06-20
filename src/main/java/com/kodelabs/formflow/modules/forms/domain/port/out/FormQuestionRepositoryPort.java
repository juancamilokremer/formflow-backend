package com.kodelabs.formflow.modules.forms.domain.port.out;

import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface FormQuestionRepositoryPort {

    FormQuestion save(FormQuestion question);

    void saveAll(List<FormQuestion> questions);

    Optional<FormQuestion> findByIdAndSectionIdAndTenantId(UUID id, UUID sectionId, UUID tenantId);

    List<FormQuestion> findActiveBySectionIdAndTenantId(UUID sectionId, UUID tenantId);

    int countActiveBySectionId(UUID sectionId);

    /** Returns active questions grouped by sectionId — single query, avoids N+1 on form detail. */
    Map<UUID, List<FormQuestion>> findAllActiveBySectionIds(List<UUID> sectionIds);

    List<FormQuestion> findActiveByFormIdAndTenantId(UUID formId, UUID tenantId);

    boolean existsActiveByCategoryIdAndTenantId(UUID categoryId, UUID tenantId);
}
