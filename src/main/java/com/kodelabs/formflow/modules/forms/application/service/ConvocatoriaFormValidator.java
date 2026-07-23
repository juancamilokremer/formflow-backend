package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConvocatoriaFormValidator {

    private final FormRepositoryPort formRepository;

    public void validateExists(UUID formId, UUID tenantId) {
        if (formId == null) return;
        if (!formRepository.existsByIdAndTenantId(formId, tenantId)) {
            throw new BusinessException("error.form.not_found", HttpStatus.NOT_FOUND, formId);
        }
    }
}
