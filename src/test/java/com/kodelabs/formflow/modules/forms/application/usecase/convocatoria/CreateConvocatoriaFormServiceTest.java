package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.FormCloner;
import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.CreateConvocatoriaFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaFormResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaFormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import com.kodelabs.formflow.shared.i18n.Messages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateConvocatoriaFormServiceTest {

    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private ConvocatoriaFormRepositoryPort convocatoriaFormRepository;
    @Mock private FormRepositoryPort formRepository;
    @Mock private FormLoader formLoader;
    @Mock private FormCloner formCloner;
    @Mock private Messages messages;
    @InjectMocks private CreateConvocatoriaFormService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId   = UUID.randomUUID();
    private final UUID convId   = UUID.randomUUID();
    private final UUID formId   = UUID.randomUUID();

    @Test
    void createsBlankFormAlreadyAttachedAtNextPosition() {
        when(convocatoriaRepository.findByIdAndTenantIdForUpdate(convId, tenantId))
                .thenReturn(Optional.of(draftConvocatoria(List.of())));
        when(formRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0)));
        when(convocatoriaFormRepository.countByConvocatoriaId(convId)).thenReturn(1);
        when(convocatoriaFormRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<CategoryWeight> weights = List.of(new CategoryWeight(UUID.randomUUID(), 100));
        ConvocatoriaFormResult result = service.execute(new CreateConvocatoriaFormCommand(
                convId, tenantId, userId, "Nuevo formulario", FormType.CANDIDATES, null, 60, weights, 50));

        assertThat(result.weight()).isEqualTo(60);
        assertThat(result.minScore()).isEqualTo(50);
        assertThat(result.position()).isEqualTo(1);

        // The form and its link are written in the same call — there is no orphan window.
        ArgumentCaptor<Form> formCaptor = ArgumentCaptor.forClass(Form.class);
        verify(formRepository).save(formCaptor.capture());
        assertThat(formCaptor.getValue().getName()).isEqualTo("Nuevo formulario");
        assertThat(formCaptor.getValue().getType()).isEqualTo(FormType.CANDIDATES);

        ArgumentCaptor<ConvocatoriaForm> linkCaptor = ArgumentCaptor.forClass(ConvocatoriaForm.class);
        verify(convocatoriaFormRepository).save(linkCaptor.capture());
        assertThat(linkCaptor.getValue().getConvocatoriaId()).isEqualTo(convId);
        assertThat(linkCaptor.getValue().getFormId()).isEqualTo(formCaptor.getValue().getId());
    }

    @Test
    void fallsBackToTheConvocatoriaTypeWhenNoneIsGiven() {
        when(convocatoriaRepository.findByIdAndTenantIdForUpdate(convId, tenantId))
                .thenReturn(Optional.of(draftConvocatoria(List.of())));
        when(formRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0)));
        when(convocatoriaFormRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute(new CreateConvocatoriaFormCommand(
                convId, tenantId, userId, "Sin tipo", null, null, 100, List.of(), null));

        ArgumentCaptor<Form> captor = ArgumentCaptor.forClass(Form.class);
        verify(formRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(FormType.CANDIDATES);
    }

    @Test
    void duplicatesAnExistingFormAndAttachesTheCopy() {
        UUID originId = UUID.randomUUID();
        Form origin = Form.builder().id(originId).tenantId(tenantId).name("Original")
                .type(FormType.CANDIDATES).build();
        Form copy = Form.builder().id(UUID.randomUUID()).tenantId(tenantId).name("Original (copia)")
                .type(FormType.CANDIDATES).build();

        when(convocatoriaRepository.findByIdAndTenantIdForUpdate(convId, tenantId))
                .thenReturn(Optional.of(draftConvocatoria(List.of())));
        when(formLoader.loadWithSectionsOrThrow(originId, tenantId)).thenReturn(origin);
        when(messages.get("form.duplicate_name_suffix", "Original")).thenReturn("Original (copia)");
        when(formCloner.clone(eq(origin), eq(userId), isNull(), isNull(), eq(1), eq("Original (copia)")))
                .thenReturn(copy);
        when(convocatoriaFormRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ConvocatoriaFormResult result = service.execute(new CreateConvocatoriaFormCommand(
                convId, tenantId, userId, null, null, originId, 40, List.of(), null));

        assertThat(result.formId()).isEqualTo(copy.getId());
        // The copy is cloned, never created blank.
        verify(formRepository, never()).save(any());
    }

    @Test
    void throwsBadRequestWhenRegistrationAlreadyHasOneForm() {
        Convocatoria encuesta = Convocatoria.builder().id(convId).tenantId(tenantId)
                .type(FormType.REGISTRATION)
                .forms(List.of(ConvocatoriaForm.builder().formId(formId).weight(100).build()))
                .name("Encuesta de clima").status(ConvocatoriaStatus.DRAFT).build();
        when(convocatoriaRepository.findByIdAndTenantIdForUpdate(convId, tenantId)).thenReturn(Optional.of(encuesta));

        var command = new CreateConvocatoriaFormCommand(
                convId, tenantId, userId, "Otro", null, null, 100, List.of(), null);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.convocatoria.registration_single_form_only")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(formRepository, never()).save(any());
    }

    @Test
    void throwsConflictWhenConvocatoriaIsNotDraft() {
        Convocatoria active = draftConvocatoria(List.of());
        active.launch();
        when(convocatoriaRepository.findByIdAndTenantIdForUpdate(convId, tenantId)).thenReturn(Optional.of(active));

        var command = new CreateConvocatoriaFormCommand(
                convId, tenantId, userId, "Nuevo", null, null, 60, List.of(), null);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.convocatoria.not_draft")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        // Nothing is written when the guard rejects: no form, no link.
        verify(formRepository, never()).save(any());
        verify(convocatoriaFormRepository, never()).save(any());
    }

    @Test
    void throwsNotFoundWhenConvocatoriaDoesNotExist() {
        when(convocatoriaRepository.findByIdAndTenantIdForUpdate(convId, tenantId)).thenReturn(Optional.empty());

        var command = new CreateConvocatoriaFormCommand(
                convId, tenantId, userId, "Nuevo", null, null, 60, List.of(), null);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.convocatoria.not_found")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(formRepository, never()).save(any());
    }

    private Convocatoria draftConvocatoria(List<ConvocatoriaForm> forms) {
        return Convocatoria.builder().id(convId).tenantId(tenantId).type(FormType.CANDIDATES)
                .forms(forms).name("Test").status(ConvocatoriaStatus.DRAFT).build();
    }

    private Form withId(Form form) {
        form.setId(UUID.randomUUID());
        return form;
    }
}
