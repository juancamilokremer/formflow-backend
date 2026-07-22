package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FormLoader {

    private final FormRepositoryPort formRepository;

    public Form loadOrThrow(UUID formId, UUID tenantId) {
        return formRepository.findByIdAndTenantId(formId, tenantId)
                .orElseThrow(() -> new BusinessException("error.form.not_found", HttpStatus.NOT_FOUND, formId));
    }

    public Form loadPublicOrThrow(UUID formId) {
        return formRepository.findByIdPublicWithSections(formId)
                .orElseThrow(() -> new BusinessException("error.form.not_found", HttpStatus.NOT_FOUND, formId));
    }

    public Form loadWithSectionsOrThrow(UUID formId, UUID tenantId) {
        return formRepository.findByIdAndTenantIdWithSections(formId, tenantId)
                .orElseThrow(() -> new BusinessException("error.form.not_found", HttpStatus.NOT_FOUND, formId));
    }
}
