package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.ConvocatoriaFormValidator;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.AddConvocatoriaFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaFormResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaFormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddConvocatoriaFormServiceTest {

    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private ConvocatoriaFormRepositoryPort convocatoriaFormRepository;
    @Mock private ConvocatoriaFormValidator formValidator;
    @InjectMocks private AddConvocatoriaFormService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId   = UUID.randomUUID();
    private final UUID convId   = UUID.randomUUID();
    private final UUID formId   = UUID.randomUUID();

    @Test
    void addsFormAtNextPosition() {
        Convocatoria draft = draftConvocatoria(List.of());
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draft));
        when(convocatoriaFormRepository.countByConvocatoriaId(convId)).thenReturn(1);
        when(convocatoriaFormRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<CategoryWeight> weights = List.of(new CategoryWeight(UUID.randomUUID(), 100));
        var command = new AddConvocatoriaFormCommand(convId, tenantId, userId, formId, 60, weights, 50);

        ConvocatoriaFormResult result = service.execute(command);

        assertThat(result.formId()).isEqualTo(formId);
        assertThat(result.weight()).isEqualTo(60);
        assertThat(result.minScore()).isEqualTo(50);
        assertThat(result.position()).isEqualTo(1);

        ArgumentCaptor<ConvocatoriaForm> captor = ArgumentCaptor.forClass(ConvocatoriaForm.class);
        verify(convocatoriaFormRepository).save(captor.capture());
        assertThat(captor.getValue().getConvocatoriaId()).isEqualTo(convId);
    }

    @Test
    void throwsConflictWhenFormAlreadyAttached() {
        Convocatoria draft = draftConvocatoria(List.of(
                ConvocatoriaForm.builder().formId(formId).weight(100).build()));
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draft));

        var command = new AddConvocatoriaFormCommand(convId, tenantId, userId, formId, 60, List.of(), null);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void throwsNotFoundWhenFormDoesNotExist() {
        Convocatoria draft = draftConvocatoria(List.of());
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draft));
        doThrow(new BusinessException("error.form.not_found", HttpStatus.NOT_FOUND, formId))
                .when(formValidator).validateExists(formId, tenantId);

        var command = new AddConvocatoriaFormCommand(convId, tenantId, userId, formId, 60, List.of(), null);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void throwsConflictWhenConvocatoriaIsNotDraft() {
        Convocatoria active = draftConvocatoria(List.of());
        active.launch();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(active));

        var command = new AddConvocatoriaFormCommand(convId, tenantId, userId, formId, 60, List.of(), null);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void throwsNotFoundWhenConvocatoriaDoesNotExist() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.empty());

        var command = new AddConvocatoriaFormCommand(convId, tenantId, userId, formId, 60, List.of(), null);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private Convocatoria draftConvocatoria(List<ConvocatoriaForm> forms) {
        return Convocatoria.builder().id(convId).tenantId(tenantId)
                .forms(forms).name("Test").status(ConvocatoriaStatus.DRAFT).build();
    }
}
