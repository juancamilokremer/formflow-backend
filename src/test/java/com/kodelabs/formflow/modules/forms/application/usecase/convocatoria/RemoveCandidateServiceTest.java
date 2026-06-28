package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.RemoveCandidateCommand;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveCandidateServiceTest {

    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private CandidateRepositoryPort candidateRepository;
    @InjectMocks private RemoveCandidateService service;

    private final UUID tenantId     = UUID.randomUUID();
    private final UUID userId       = UUID.randomUUID();
    private final UUID convId       = UUID.randomUUID();
    private final UUID candidateId  = UUID.randomUUID();

    @Test
    void deletesCandidateWhenConvocatoriaIsEditable() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId))
                .thenReturn(Optional.of(activeConvocatoria()));
        Candidate candidate = Candidate.builder().id(candidateId).convocatoriaId(convId)
                .tenantId(tenantId).name("María G.").email("maria@test.com")
                .status(CandidateStatus.INVITED).token(UUID.randomUUID()).build();
        when(candidateRepository.findByIdAndConvocatoriaId(candidateId, convId))
                .thenReturn(Optional.of(candidate));

        service.execute(new RemoveCandidateCommand(convId, candidateId, tenantId, userId));

        verify(candidateRepository).deleteById(candidateId);
    }

    @Test
    void throwsNotFoundWhenConvocatoriaDoesNotExist() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.empty());

        var command = new RemoveCandidateCommand(convId, candidateId, tenantId, userId);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void throwsConflictWhenConvocatoriaIsClosed() {
        Convocatoria closed = activeConvocatoria();
        closed.close();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(closed));

        var command = new RemoveCandidateCommand(convId, candidateId, tenantId, userId);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void throwsNotFoundWhenCandidateDoesNotExist() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId))
                .thenReturn(Optional.of(activeConvocatoria()));
        when(candidateRepository.findByIdAndConvocatoriaId(candidateId, convId)).thenReturn(Optional.empty());

        var command = new RemoveCandidateCommand(convId, candidateId, tenantId, userId);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private Convocatoria activeConvocatoria() {
        Convocatoria c = Convocatoria.builder().id(convId).tenantId(tenantId)
                .formId(UUID.randomUUID()).name("Test").status(ConvocatoriaStatus.DRAFT).build();
        c.launch();
        return c;
    }
}
