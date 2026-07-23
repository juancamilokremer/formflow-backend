package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConvocatoriaFormValidatorTest {

    @Mock private FormRepositoryPort formRepository;
    @InjectMocks private ConvocatoriaFormValidator validator;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID formId   = UUID.randomUUID();

    @Test
    void doesNothingWhenFormIdIsNull() {
        assertThatCode(() -> validator.validateExists(null, tenantId)).doesNotThrowAnyException();
        verifyNoInteractions(formRepository);
    }

    @Test
    void doesNothingWhenFormExists() {
        when(formRepository.existsByIdAndTenantId(formId, tenantId)).thenReturn(true);

        assertThatCode(() -> validator.validateExists(formId, tenantId)).doesNotThrowAnyException();
    }

    @Test
    void throwsNotFoundWhenFormDoesNotExist() {
        when(formRepository.existsByIdAndTenantId(formId, tenantId)).thenReturn(false);

        assertThatThrownBy(() -> validator.validateExists(formId, tenantId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
