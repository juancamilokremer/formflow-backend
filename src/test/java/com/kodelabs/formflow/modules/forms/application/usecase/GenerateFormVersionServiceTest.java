package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.service.FormCloner;
import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.application.usecase.form.GenerateFormVersionService;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GenerateFormVersionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormSummaryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaFormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateFormVersionServiceTest {

    @Mock private FormLoader formLoader;
    @Mock private FormRepositoryPort formRepository;
    @Mock private ConvocatoriaFormRepositoryPort convocatoriaFormRepository;
    @Mock private FormCloner formCloner;
    @InjectMocks private GenerateFormVersionService service;

    private UUID formId;
    private UUID tenantId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        formId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void throwsBadRequestWhenSourceFormIsDraft() {
        Form draft = Form.builder().id(formId).tenantId(tenantId)
                .type(FormType.CANDIDATES).status(FormStatus.DRAFT).sections(List.of()).build();
        when(formLoader.loadWithSectionsOrThrow(formId, tenantId)).thenReturn(draft);

        var command = new GenerateFormVersionCommand(formId, tenantId, userId);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.form.version_source_not_locked")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(formCloner, never()).clone(any(), any(), any(), any(), anyInt());
    }

    @Test
    void throwsBadRequestWhenSourceFormIsRegistration() {
        Form registrationForm = Form.builder().id(formId).tenantId(tenantId)
                .type(FormType.REGISTRATION).status(FormStatus.ACTIVE).sections(List.of()).build();
        when(formLoader.loadWithSectionsOrThrow(formId, tenantId)).thenReturn(registrationForm);

        var command = new GenerateFormVersionCommand(formId, tenantId, userId);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.form.version_source_not_locked");

        verify(formCloner, never()).clone(any(), any(), any(), any(), anyInt());
    }

    @Test
    void clonesWithSelfAsRootWhenSourceIsTheOriginalForm() {
        Form origin = Form.builder().id(formId).tenantId(tenantId)
                .type(FormType.CANDIDATES).status(FormStatus.ARCHIVED).version(1).sections(List.of()).build();
        when(formLoader.loadWithSectionsOrThrow(formId, tenantId)).thenReturn(origin);
        when(formRepository.findMaxVersionInFamily(formId, tenantId)).thenReturn(1);
        Form cloned = Form.builder().id(UUID.randomUUID()).tenantId(tenantId)
                .type(FormType.CANDIDATES).status(FormStatus.DRAFT).version(2).build();
        when(formCloner.clone(eq(origin), eq(userId), eq(formId), eq(formId), eq(2))).thenReturn(cloned);

        FormSummaryResult result = service.execute(new GenerateFormVersionCommand(formId, tenantId, userId));

        assertThat(result.id()).isEqualTo(cloned.getId());
        assertThat(result.version()).isEqualTo(2);
    }

    @Test
    void clonesWithInheritedRootWhenSourceIsAlreadyAVersion() {
        UUID familyRoot = UUID.randomUUID();
        Form origin = Form.builder().id(formId).tenantId(tenantId)
                .type(FormType.DIAGNOSTIC).status(FormStatus.ARCHIVED).version(2)
                .rootFormId(familyRoot).sections(List.of()).build();
        when(formLoader.loadWithSectionsOrThrow(formId, tenantId)).thenReturn(origin);
        when(formRepository.findMaxVersionInFamily(familyRoot, tenantId)).thenReturn(3);
        Form cloned = Form.builder().id(UUID.randomUUID()).tenantId(tenantId)
                .type(FormType.DIAGNOSTIC).status(FormStatus.DRAFT).version(4).build();
        when(formCloner.clone(eq(origin), eq(userId), eq(formId), eq(familyRoot), eq(4))).thenReturn(cloned);

        service.execute(new GenerateFormVersionCommand(formId, tenantId, userId));

        ArgumentCaptor<UUID> rootCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(formCloner).clone(eq(origin), eq(userId), eq(formId), rootCaptor.capture(), eq(4));
        assertThat(rootCaptor.getValue()).isEqualTo(familyRoot).isNotEqualTo(formId);
    }

    @Test
    void allowsGeneratingMultipleIndependentVersionsFromTheSameOrigin() {
        Form origin = Form.builder().id(formId).tenantId(tenantId)
                .type(FormType.CANDIDATES).status(FormStatus.ARCHIVED).version(1).sections(List.of()).build();
        when(formLoader.loadWithSectionsOrThrow(formId, tenantId)).thenReturn(origin);
        when(formRepository.findMaxVersionInFamily(formId, tenantId)).thenReturn(1).thenReturn(2);
        when(formCloner.clone(any(), any(), any(), any(), anyInt()))
                .thenAnswer(inv -> Form.builder().id(UUID.randomUUID()).tenantId(tenantId)
                        .type(FormType.CANDIDATES).status(FormStatus.DRAFT)
                        .version(inv.getArgument(4)).build());

        var command = new GenerateFormVersionCommand(formId, tenantId, userId);
        FormSummaryResult first = service.execute(command);
        FormSummaryResult second = service.execute(command);

        assertThat(first.version()).isEqualTo(2);
        assertThat(second.version()).isEqualTo(3);
        assertThat(first.id()).isNotEqualTo(second.id());
    }

    @Test
    void repointsEveryContainerToTheNewVersion() {
        Form origin = Form.builder().id(formId).tenantId(tenantId)
                .type(FormType.CANDIDATES).status(FormStatus.ARCHIVED).version(1).sections(List.of()).build();
        Form cloned = Form.builder().id(UUID.randomUUID()).tenantId(tenantId)
                .type(FormType.CANDIDATES).status(FormStatus.DRAFT).version(2).build();
        when(formLoader.loadWithSectionsOrThrow(formId, tenantId)).thenReturn(origin);
        when(formRepository.findMaxVersionInFamily(formId, tenantId)).thenReturn(1);
        when(formCloner.clone(eq(origin), eq(userId), eq(formId), eq(formId), eq(2))).thenReturn(cloned);

        // The same form can sit in several convocatorias since V17 — all of them must follow.
        ConvocatoriaForm first = ConvocatoriaForm.builder().id(UUID.randomUUID())
                .convocatoriaId(UUID.randomUUID()).formId(formId).weight(60).minScore(50).position(0).build();
        ConvocatoriaForm second = ConvocatoriaForm.builder().id(UUID.randomUUID())
                .convocatoriaId(UUID.randomUUID()).formId(formId).weight(40).position(2).build();
        when(convocatoriaFormRepository.findAllByFormId(formId)).thenReturn(List.of(first, second));

        service.execute(new GenerateFormVersionCommand(formId, tenantId, userId));

        ArgumentCaptor<List<ConvocatoriaForm>> captor = ArgumentCaptor.forClass(List.class);
        verify(convocatoriaFormRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).allSatisfy(link ->
                assertThat(link.getFormId()).isEqualTo(cloned.getId()));
        // The scoring setup of each link survives the repoint.
        assertThat(captor.getValue()).extracting(ConvocatoriaForm::getWeight).containsExactly(60, 40);
        assertThat(captor.getValue()).extracting(ConvocatoriaForm::getMinScore).containsExactly(50, null);
        assertThat(captor.getValue()).extracting(ConvocatoriaForm::getPosition).containsExactly(0, 2);
    }

    @Test
    void doesNotTouchContainersWhenTheFormBelongsToNone() {
        Form origin = Form.builder().id(formId).tenantId(tenantId)
                .type(FormType.CANDIDATES).status(FormStatus.ARCHIVED).version(1).sections(List.of()).build();
        when(formLoader.loadWithSectionsOrThrow(formId, tenantId)).thenReturn(origin);
        when(formRepository.findMaxVersionInFamily(formId, tenantId)).thenReturn(1);
        when(formCloner.clone(any(), any(), any(), any(), anyInt())).thenReturn(
                Form.builder().id(UUID.randomUUID()).tenantId(tenantId).type(FormType.CANDIDATES)
                        .status(FormStatus.DRAFT).version(2).build());
        when(convocatoriaFormRepository.findAllByFormId(formId)).thenReturn(List.of());

        service.execute(new GenerateFormVersionCommand(formId, tenantId, userId));

        verify(convocatoriaFormRepository, never()).saveAll(any());
    }
}