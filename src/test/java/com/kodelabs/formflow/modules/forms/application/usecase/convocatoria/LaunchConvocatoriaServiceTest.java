package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.ConvocatoriaWeightValidator;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.LaunchConvocatoriaCommand;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LaunchConvocatoriaServiceTest {

    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private CandidateRepositoryPort candidateRepository;
    @Spy  private ConvocatoriaWeightValidator weightValidator = new ConvocatoriaWeightValidator();

    @InjectMocks private LaunchConvocatoriaService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId   = UUID.randomUUID();
    private final UUID convId   = UUID.randomUUID();

    @Test
    void launchesConvocatoriaToActiveStatus() {
        Convocatoria draft = draftConvocatoria();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draft));
        when(candidateRepository.countByConvocatoriaId(convId)).thenReturn(3L);
        when(convocatoriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(candidateRepository.findAllByConvocatoriaId(convId)).thenReturn(List.of());

        var result = service.execute(new LaunchConvocatoriaCommand(convId, tenantId, userId));

        assertThat(result.status()).isEqualTo(ConvocatoriaStatus.ACTIVE.name());
    }

    @Test
    void throwsConflictWhenAlreadyActive() {
        Convocatoria active = draftConvocatoria();
        active.launch();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(active));

        assertThatThrownBy(() -> service.execute(new LaunchConvocatoriaCommand(convId, tenantId, userId)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void throwsBadRequestWhenNoCandidates() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draftConvocatoria()));
        when(candidateRepository.countByConvocatoriaId(convId)).thenReturn(0L);

        assertThatThrownBy(() -> service.execute(new LaunchConvocatoriaCommand(convId, tenantId, userId)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void throwsNotFoundWhenConvocatoriaDoesNotExist() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new LaunchConvocatoriaCommand(convId, tenantId, userId)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private Convocatoria draftConvocatoria() {
        return Convocatoria.builder().id(convId).tenantId(tenantId).formId(UUID.randomUUID())
                .name("Test").status(ConvocatoriaStatus.DRAFT).build();
    }
}
