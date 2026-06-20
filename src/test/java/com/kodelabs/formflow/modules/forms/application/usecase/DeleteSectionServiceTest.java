package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.usecase.section.DeleteSectionService;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.DeleteSectionCommand;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteSectionServiceTest {

    @Mock private FormRepositoryPort formRepository;
    @Mock private FormSectionRepositoryPort sectionRepository;
    @InjectMocks private DeleteSectionService service;

    private UUID formId;
    private UUID tenantId;
    private UUID userId;
    private UUID sectionId;
    private FormSection section;
    private Form form;

    @BeforeEach
    void setUp() {
        formId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        sectionId = UUID.randomUUID();
        section = FormSection.builder().id(sectionId).formId(formId).tenantId(tenantId).title("S").position(0).build();
        form = Form.builder().id(formId).tenantId(tenantId).name("F").type(FormType.CANDIDATES).version(1).build();
    }

    @Test
    void softDeletesSectionAndIncrementsFormVersion() {
        when(sectionRepository.findByIdAndFormIdAndTenantId(sectionId, formId, tenantId))
                .thenReturn(Optional.of(section));
        when(sectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.of(form));
        when(formRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute(new DeleteSectionCommand(sectionId, formId, tenantId, userId));

        ArgumentCaptor<FormSection> sectionCaptor = ArgumentCaptor.forClass(FormSection.class);
        verify(sectionRepository).save(sectionCaptor.capture());
        assertThat(sectionCaptor.getValue().isDeleted()).isTrue();

        ArgumentCaptor<Form> formCaptor = ArgumentCaptor.forClass(Form.class);
        verify(formRepository).save(formCaptor.capture());
        assertThat(formCaptor.getValue().getVersion()).isEqualTo(2);
    }

    @Test
    void throwsNotFoundWhenSectionDoesNotExist() {
        when(sectionRepository.findByIdAndFormIdAndTenantId(sectionId, formId, tenantId))
                .thenReturn(Optional.empty());

        var command = new DeleteSectionCommand(sectionId, formId, tenantId, userId);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.section.not_found")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(formRepository, never()).findByIdAndTenantId(any(), any());
    }
}
