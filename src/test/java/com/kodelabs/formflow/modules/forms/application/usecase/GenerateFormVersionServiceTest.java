package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.service.FormCloner;
import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.application.usecase.form.GenerateFormVersionService;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GenerateFormVersionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormSummaryResult;
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
}
