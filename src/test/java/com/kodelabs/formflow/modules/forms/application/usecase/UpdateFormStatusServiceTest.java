package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.usecase.form.UpdateFormStatusService;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateFormStatusCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormSummaryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateFormStatusServiceTest {

    @Mock private FormRepositoryPort formRepository;
    @Mock private FormSectionRepositoryPort sectionRepository;
    @Mock private FormResponseRepositoryPort responseRepository;
    @InjectMocks private UpdateFormStatusService service;

    private UUID formId;
    private UUID tenantId;
    private UUID userId;
    private Form form;

    @BeforeEach
    void setUp() {
        formId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        form = Form.builder().id(formId).tenantId(tenantId).name("Evaluación")
                .type(FormType.CANDIDATES).status(FormStatus.DRAFT).version(1).build();
    }

    @Test
    void updatesStatusAndReturnsResult() {
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.of(form));
        when(formRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sectionRepository.countActiveByFormId(formId)).thenReturn(3);
        when(responseRepository.countByFormIds(List.of(formId))).thenReturn(Map.of(formId, 5));
        when(responseRepository.lastResponseAtByFormIds(List.of(formId))).thenReturn(Map.of());

        FormSummaryResult result = service.execute(
                new UpdateFormStatusCommand(formId, tenantId, userId, FormStatus.ACTIVE));

        assertThat(result.status()).isEqualTo(FormStatus.ACTIVE);
        assertThat(result.sectionCount()).isEqualTo(3);
        assertThat(result.responseCount()).isEqualTo(5);
    }

    @Test
    void throwsNotFoundWhenFormDoesNotBelongToTenant() {
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.empty());

        var command = new UpdateFormStatusCommand(formId, tenantId, userId, FormStatus.ACTIVE);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
