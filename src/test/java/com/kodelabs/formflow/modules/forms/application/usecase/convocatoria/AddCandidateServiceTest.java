package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.AddCandidateCommand;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddCandidateServiceTest {

    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private CandidateRepositoryPort candidateRepository;

    @InjectMocks private AddCandidateService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId   = UUID.randomUUID();
    private final UUID convId   = UUID.randomUUID();

    @Test
    void addsCandidateWithInvitedStatus() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId))
                .thenReturn(Optional.of(activeConvocatoria()));
        when(candidateRepository.existsByConvocatoriaIdAndEmail(convId, "maria@test.com")).thenReturn(false);
        Candidate saved = Candidate.builder().id(UUID.randomUUID()).convocatoriaId(convId)
                .tenantId(tenantId).name("María G.").email("maria@test.com")
                .status(CandidateStatus.INVITED).token(UUID.randomUUID()).build();
        when(candidateRepository.save(any())).thenReturn(saved);

        var result = service.execute(new AddCandidateCommand(convId, tenantId, userId, "María G.", "maria@test.com"));

        assertThat(result.name()).isEqualTo("María G.");
        assertThat(result.status()).isEqualTo(CandidateStatus.INVITED.name());
    }

    @Test
    void throwsConflictOnDuplicateEmail() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId))
                .thenReturn(Optional.of(activeConvocatoria()));
        when(candidateRepository.existsByConvocatoriaIdAndEmail(convId, "dup@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.execute(
                new AddCandidateCommand(convId, tenantId, userId, "Dup", "dup@test.com")))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void throwsConflictWhenConvocatoriaIsClosed() {
        Convocatoria closed = activeConvocatoria();
        closed.close();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(closed));

        assertThatThrownBy(() -> service.execute(
                new AddCandidateCommand(convId, tenantId, userId, "Test", "test@test.com")))
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
