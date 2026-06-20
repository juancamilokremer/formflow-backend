package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.usecase.section.AddSectionService;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.AddSectionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SectionResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddSectionServiceTest {

    @Mock private FormRepositoryPort formRepository;
    @Mock private FormSectionRepositoryPort sectionRepository;
    @InjectMocks private AddSectionService service;

    private UUID formId;
    private UUID tenantId;
    private UUID userId;
    private Form form;

    @BeforeEach
    void setUp() {
        formId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        form = Form.builder().id(formId).tenantId(tenantId).name("F").type(FormType.CANDIDATES).version(1).build();
    }

    @Test
    void appendsSectionAtNextPositionAndIncrementsFormVersion() {
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.of(form));
        when(sectionRepository.countActiveByFormId(formId)).thenReturn(2);
        FormSection saved = FormSection.builder().id(UUID.randomUUID()).formId(formId).title("S3").position(2).build();
        when(sectionRepository.save(any())).thenReturn(saved);
        when(formRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SectionResult result = service.execute(new AddSectionCommand(formId, tenantId, userId, "S3", null, null));

        assertThat(result.title()).isEqualTo("S3");
        assertThat(result.position()).isEqualTo(2);

        ArgumentCaptor<Form> formCaptor = ArgumentCaptor.forClass(Form.class);
        verify(formRepository).save(formCaptor.capture());
        assertThat(formCaptor.getValue().getVersion()).isEqualTo(2);
    }

    @Test
    void savesSectionWithCorrectPositionWhenFormIsEmpty() {
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.of(form));
        when(sectionRepository.countActiveByFormId(formId)).thenReturn(0);
        when(sectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(formRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute(new AddSectionCommand(formId, tenantId, userId, "Primera", null, null));

        ArgumentCaptor<FormSection> captor = ArgumentCaptor.forClass(FormSection.class);
        verify(sectionRepository).save(captor.capture());
        assertThat(captor.getValue().getPosition()).isZero();
    }

    @Test
    void propagatesTimeLimitSecondsToPersistedSection() {
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.of(form));
        when(sectionRepository.countActiveByFormId(formId)).thenReturn(0);
        when(sectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(formRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute(new AddSectionCommand(formId, tenantId, userId, "Timed", null, 90));

        ArgumentCaptor<FormSection> captor = ArgumentCaptor.forClass(FormSection.class);
        verify(sectionRepository).save(captor.capture());
        assertThat(captor.getValue().getTimeLimitSeconds()).isEqualTo(90);
    }

    @Test
    void throwsNotFoundWhenFormDoesNotBelongToTenant() {
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.empty());

        var command = new AddSectionCommand(formId, tenantId, userId, "S", null, null);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.form.not_found")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
