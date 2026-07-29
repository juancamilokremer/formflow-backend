package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.RemoveConvocatoriaFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaFormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveConvocatoriaFormServiceTest {

    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private ConvocatoriaFormRepositoryPort convocatoriaFormRepository;
    @InjectMocks private RemoveConvocatoriaFormService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId   = UUID.randomUUID();
    private final UUID convId   = UUID.randomUUID();
    private final UUID convFormId = UUID.randomUUID();

    @Test
    void removesFormFromDraftConvocatoria() {
        ConvocatoriaForm existing = ConvocatoriaForm.builder()
                .id(convFormId).convocatoriaId(convId).formId(UUID.randomUUID()).weight(100).build();
        Convocatoria draft = draftConvocatoria(List.of(existing));
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draft));

        service.execute(new RemoveConvocatoriaFormCommand(convFormId, convId, tenantId, userId));

        verify(convocatoriaFormRepository).deleteById(convFormId);
    }

    @Test
    void throwsNotFoundWhenFormDoesNotBelongToConvocatoria() {
        Convocatoria draft = draftConvocatoria(List.of());
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draft));

        var command = new RemoveConvocatoriaFormCommand(convFormId, convId, tenantId, userId);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void throwsConflictWhenConvocatoriaIsNotDraft() {
        Convocatoria active = draftConvocatoria(List.of());
        active.launch();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(active));

        var command = new RemoveConvocatoriaFormCommand(convFormId, convId, tenantId, userId);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void throwsNotFoundWhenConvocatoriaDoesNotExist() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.empty());

        var command = new RemoveConvocatoriaFormCommand(convFormId, convId, tenantId, userId);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private Convocatoria draftConvocatoria(List<ConvocatoriaForm> forms) {
        return Convocatoria.builder().id(convId).tenantId(tenantId)
                .forms(forms).name("Test").status(ConvocatoriaStatus.DRAFT).build();
    }
}
