package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.application.usecase.section.UpdateSectionService;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateSectionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SectionResult;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateSectionServiceTest {

    @Mock private FormLoader formLoader;
    @Mock private FormSectionRepositoryPort sectionRepository;
    @InjectMocks private UpdateSectionService service;

    private UUID sectionId;
    private UUID formId;
    private UUID tenantId;
    private Form draftForm;

    @BeforeEach
    void setUp() {
        sectionId = UUID.randomUUID();
        formId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        draftForm = Form.builder().id(formId).tenantId(tenantId).name("F").type(FormType.CANDIDATES).version(1).build();
    }

    private Form lockedForm() {
        return Form.builder().id(formId).tenantId(tenantId).name("F")
                .type(FormType.CANDIDATES).status(FormStatus.ACTIVE).version(2).build();
    }

    @Test
    void updatesTitleDescriptionAndTimeLimitSeconds() {
        FormSection section = FormSection.builder().id(sectionId).formId(formId).title("Viejo").position(0).build();
        when(sectionRepository.findByIdAndFormIdAndTenantId(sectionId, formId, tenantId))
                .thenReturn(Optional.of(section));
        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(draftForm);
        when(sectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SectionResult result = service.execute(new UpdateSectionCommand(sectionId, formId, tenantId, "Nuevo", "Desc", 120));

        assertThat(result.title()).isEqualTo("Nuevo");
        assertThat(result.description()).isEqualTo("Desc");
        assertThat(result.timeLimitSeconds()).isEqualTo(120);
    }

    @Test
    void clearsTimeLimitSecondsWhenNullIsProvided() {
        FormSection section = FormSection.builder().id(sectionId).formId(formId).title("S")
                .position(0).timeLimitSeconds(60).build();
        when(sectionRepository.findByIdAndFormIdAndTenantId(sectionId, formId, tenantId))
                .thenReturn(Optional.of(section));
        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(draftForm);
        when(sectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SectionResult result = service.execute(new UpdateSectionCommand(sectionId, formId, tenantId, "S", null, null));

        assertThat(result.timeLimitSeconds()).isNull();
    }

    @Test
    void throwsNotFoundWhenSectionDoesNotExist() {
        when(sectionRepository.findByIdAndFormIdAndTenantId(sectionId, formId, tenantId))
                .thenReturn(Optional.empty());

        var command = new UpdateSectionCommand(sectionId, formId, tenantId, "T", null, null);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.section.not_found")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void throwsBadRequestWhenFormIsLockedAndTimeLimitChanges() {
        FormSection section = FormSection.builder().id(sectionId).formId(formId).title("S")
                .position(0).timeLimitSeconds(60).build();
        when(sectionRepository.findByIdAndFormIdAndTenantId(sectionId, formId, tenantId))
                .thenReturn(Optional.of(section));
        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(lockedForm());

        var command = new UpdateSectionCommand(sectionId, formId, tenantId, "S", null, 180);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.question.form_locked")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(sectionRepository, never()).save(any());
    }

    @Test
    void allowsCosmeticOnlyChangeWhenFormIsLocked() {
        FormSection section = FormSection.builder().id(sectionId).formId(formId).title("Viejo")
                .position(0).timeLimitSeconds(60).build();
        when(sectionRepository.findByIdAndFormIdAndTenantId(sectionId, formId, tenantId))
                .thenReturn(Optional.of(section));
        when(sectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Same time limit as the stored section — only title/description change, so the form is never loaded.
        SectionResult result = service.execute(
                new UpdateSectionCommand(sectionId, formId, tenantId, "Nuevo", "Desc", 60));

        assertThat(result.title()).isEqualTo("Nuevo");
        assertThat(result.description()).isEqualTo("Desc");
        verify(formLoader, never()).loadOrThrow(any(), any());
    }
}
