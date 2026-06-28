package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.CloseConvocatoriaCommand;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseConvocatoriaServiceTest {

    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private CandidateRepositoryPort candidateRepository;
    @InjectMocks private CloseConvocatoriaService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId   = UUID.randomUUID();
    private final UUID convId   = UUID.randomUUID();

    @Test
    void closesActiveConvocatoriaAndReturnsClosedStatus() {
        Convocatoria active = activeConvocatoria();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(active));
        when(convocatoriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(candidateRepository.findAllByConvocatoriaId(convId)).thenReturn(List.of());

        ConvocatoriaResult result = service.execute(new CloseConvocatoriaCommand(convId, tenantId, userId));

        assertThat(result.status()).isEqualTo(ConvocatoriaStatus.CLOSED.name());
    }

    @Test
    void throwsNotFoundWhenConvocatoriaDoesNotExist() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.empty());

        var command = new CloseConvocatoriaCommand(convId, tenantId, userId);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void throwsConflictWhenConvocatoriaIsNotActive() {
        Convocatoria draft = Convocatoria.builder().id(convId).tenantId(tenantId)
                .formId(UUID.randomUUID()).name("Test").status(ConvocatoriaStatus.DRAFT).build();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draft));

        var command = new CloseConvocatoriaCommand(convId, tenantId, userId);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    private Convocatoria activeConvocatoria() {
        Convocatoria c = Convocatoria.builder().id(convId).tenantId(tenantId)
                .formId(UUID.randomUUID()).name("Test").status(ConvocatoriaStatus.DRAFT).build();
        c.launch();
        return c;
    }
}
