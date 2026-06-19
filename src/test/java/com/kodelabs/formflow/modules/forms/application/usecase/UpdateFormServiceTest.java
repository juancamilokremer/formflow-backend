package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.usecase.form.UpdateFormService;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormSummaryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateFormServiceTest {

    @Mock private FormRepositoryPort formRepository;
    @Mock private FormSectionRepositoryPort sectionRepository;
    @InjectMocks private UpdateFormService service;

    private UUID formId;
    private UUID tenantId;
    private UUID userId;
    private Form existing;

    @BeforeEach
    void setUp() {
        formId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        existing = Form.builder().id(formId).tenantId(tenantId).name("Viejo").type(FormType.CANDIDATES).version(1).build();
    }

    @Test
    void updatesMetadataWithoutIncrementingVersion() {
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.of(existing));
        when(formRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sectionRepository.countActiveByFormId(formId)).thenReturn(2);

        FormSummaryResult result = service.execute(
                new UpdateFormCommand(formId, tenantId, userId, "Nuevo nombre", "Desc", 300));

        assertThat(result.name()).isEqualTo("Nuevo nombre");
        assertThat(result.version()).isEqualTo(1);
        assertThat(result.sectionCount()).isEqualTo(2);
    }

    @Test
    void setsUpdatedByFromCommand() {
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.of(existing));
        when(formRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sectionRepository.countActiveByFormId(any())).thenReturn(0);

        service.execute(new UpdateFormCommand(formId, tenantId, userId, "N", null, null));

        assertThat(existing.getUpdatedBy()).isEqualTo(userId);
    }

    @Test
    void throwsNotFoundWhenFormDoesNotBelongToTenant() {
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(
                new UpdateFormCommand(formId, tenantId, userId, "N", null, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.form.not_found")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
