package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.ConvocatoriaWeightValidator;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateConvocatoriaCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateConvocatoriaServiceTest {

    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private CandidateRepositoryPort candidateRepository;
    @Mock private ConvocatoriaWeightValidator weightValidator;
    @InjectMocks private UpdateConvocatoriaService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId   = UUID.randomUUID();
    private final UUID convId   = UUID.randomUUID();

    @Test
    void updatesDraftConvocatoriaNameAndWeights() {
        Convocatoria draft = draftConvocatoria();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draft));
        when(convocatoriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(candidateRepository.findAllByConvocatoriaId(convId)).thenReturn(List.of());
        List<CategoryWeight> weights = List.of(
                new CategoryWeight(UUID.randomUUID(), 60),
                new CategoryWeight(UUID.randomUUID(), 40));

        ConvocatoriaResult result = service.execute(
                new UpdateConvocatoriaCommand(convId, tenantId, userId, "Proceso Actualizado", weights, null));

        assertThat(result.name()).isEqualTo("Proceso Actualizado");
        assertThat(result.categoryWeights()).hasSize(2);
    }

    @Test
    void throwsNotFoundWhenConvocatoriaDoesNotExist() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.empty());

        var command = new UpdateConvocatoriaCommand(convId, tenantId, userId, "X", null, null);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void throwsConflictWhenConvocatoriaIsNotDraft() {
        Convocatoria active = draftConvocatoria();
        active.launch();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(active));

        var command = new UpdateConvocatoriaCommand(convId, tenantId, userId, "X", null, null);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void throwsBadRequestWhenWeightValidationFails() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draftConvocatoria()));
        List<CategoryWeight> invalidWeights = List.of(new CategoryWeight(UUID.randomUUID(), 50));
        doThrow(new BusinessException("error.convocatoria.weights_must_sum_100", HttpStatus.BAD_REQUEST, 50))
                .when(weightValidator).validate(invalidWeights);

        var command = new UpdateConvocatoriaCommand(convId, tenantId, userId, "X", invalidWeights, null);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    private Convocatoria draftConvocatoria() {
        return Convocatoria.builder().id(convId).tenantId(tenantId)
                .formId(UUID.randomUUID()).name("Proceso Original").status(ConvocatoriaStatus.DRAFT).build();
    }
}
