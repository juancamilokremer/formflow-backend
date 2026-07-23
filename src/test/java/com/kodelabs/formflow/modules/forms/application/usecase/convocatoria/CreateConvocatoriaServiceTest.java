package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.ConvocatoriaFormValidator;
import com.kodelabs.formflow.modules.forms.application.service.ConvocatoriaWeightValidator;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ScoringConfig;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.CreateConvocatoriaCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateConvocatoriaServiceTest {

    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private ConvocatoriaFormValidator formValidator;
    @Spy  private ConvocatoriaWeightValidator weightValidator = new ConvocatoriaWeightValidator();

    @InjectMocks private CreateConvocatoriaService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId   = UUID.randomUUID();
    private final UUID formId   = UUID.randomUUID();

    @Test
    void createsConvocatoriaInDraftStatus() {
        when(convocatoriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateConvocatoriaCommand command = new CreateConvocatoriaCommand(
                tenantId, userId, formId, "Analista RRHH", FormType.CANDIDATES, List.of(), null);

        ConvocatoriaResult result = service.execute(command);

        assertThat(result.name()).isEqualTo("Analista RRHH");
        assertThat(result.type()).isEqualTo(FormType.CANDIDATES);
        assertThat(result.status()).isEqualTo(ConvocatoriaStatus.DRAFT.name());
        assertThat(result.candidates()).isEmpty();
    }

    @Test
    void createsConvocatoriaWithoutFormId() {
        when(convocatoriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateConvocatoriaCommand command = new CreateConvocatoriaCommand(
                tenantId, userId, null, "Analista RRHH", FormType.DIAGNOSTIC, List.of(), null);

        ConvocatoriaResult result = service.execute(command);

        assertThat(result.formId()).isNull();
        assertThat(result.type()).isEqualTo(FormType.DIAGNOSTIC);
        verify(formValidator).validateExists(null, tenantId);
    }

    @Test
    void throwsNotFoundWhenFormDoesNotExist() {
        doThrow(new BusinessException("error.form.not_found", HttpStatus.NOT_FOUND, formId))
                .when(formValidator).validateExists(formId, tenantId);

        CreateConvocatoriaCommand command = new CreateConvocatoriaCommand(
                tenantId, userId, formId, "Test", FormType.CANDIDATES, List.of(), null);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void throwsBadRequestWhenTypeIsRegistration() {
        CreateConvocatoriaCommand command = new CreateConvocatoriaCommand(
                tenantId, userId, formId, "Test", FormType.REGISTRATION, List.of(), null);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void throwsBadRequestWhenWeightsDoNotSumTo100() {
        List<CategoryWeight> weights = List.of(
                new CategoryWeight(UUID.randomUUID(), 40),
                new CategoryWeight(UUID.randomUUID(), 30));

        CreateConvocatoriaCommand command = new CreateConvocatoriaCommand(
                tenantId, userId, formId, "Test", FormType.CANDIDATES, weights, null);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void acceptsWeightsThatSumTo100() {
        ArgumentCaptor<Convocatoria> captor = ArgumentCaptor.forClass(Convocatoria.class);
        when(convocatoriaRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        List<CategoryWeight> weights = List.of(
                new CategoryWeight(UUID.randomUUID(), 60),
                new CategoryWeight(UUID.randomUUID(), 40));

        service.execute(new CreateConvocatoriaCommand(
                tenantId, userId, formId, "Test", FormType.CANDIDATES, weights, ScoringConfig.defaults()));

        assertThat(captor.getValue().getCategoryWeights()).hasSize(2);
        verify(convocatoriaRepository).save(any());
    }

    @Test
    void usesScoringConfigDefaultsWhenNotProvided() {
        ArgumentCaptor<Convocatoria> captor = ArgumentCaptor.forClass(Convocatoria.class);
        when(convocatoriaRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.execute(new CreateConvocatoriaCommand(
                tenantId, userId, formId, "Test", FormType.CANDIDATES, null, null));

        assertThat(captor.getValue().getScoringConfig().aptoMin()).isEqualTo(70);
        assertThat(captor.getValue().getScoringConfig().revisarMin()).isEqualTo(50);
    }
}
