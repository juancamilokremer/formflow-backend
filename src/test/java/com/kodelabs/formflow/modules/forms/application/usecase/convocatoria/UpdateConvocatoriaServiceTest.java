package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ScoringConfig;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateConvocatoriaServiceTest {

    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private CandidateRepositoryPort candidateRepository;
    @InjectMocks private UpdateConvocatoriaService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId   = UUID.randomUUID();
    private final UUID convId   = UUID.randomUUID();

    @Test
    void updatesDraftConvocatoriaName() {
        Convocatoria draft = draftConvocatoria();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draft));
        when(convocatoriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(candidateRepository.findAllByConvocatoriaId(convId)).thenReturn(List.of());

        ConvocatoriaResult result = service.execute(
                new UpdateConvocatoriaCommand(convId, tenantId, userId, "Proceso Actualizado", null));

        assertThat(result.name()).isEqualTo("Proceso Actualizado");
    }

    @Test
    void updatesScoringConfigWhenProvided() {
        Convocatoria draft = draftConvocatoria();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draft));
        when(convocatoriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(candidateRepository.findAllByConvocatoriaId(convId)).thenReturn(List.of());

        ConvocatoriaResult result = service.execute(
                new UpdateConvocatoriaCommand(convId, tenantId, userId, "Proceso Original", new ScoringConfig(80, 60)));

        assertThat(result.scoringConfig().aptoMin()).isEqualTo(80);
        assertThat(result.scoringConfig().revisarMin()).isEqualTo(60);
    }

    @Test
    void leavesScoringConfigUntouchedWhenNotProvided() {
        Convocatoria draft = draftConvocatoria();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draft));
        when(convocatoriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(candidateRepository.findAllByConvocatoriaId(convId)).thenReturn(List.of());

        ConvocatoriaResult result = service.execute(
                new UpdateConvocatoriaCommand(convId, tenantId, userId, "Proceso Original", null));

        assertThat(result.scoringConfig()).isEqualTo(ScoringConfig.defaults());
    }

    @Test
    void throwsNotFoundWhenConvocatoriaDoesNotExist() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.empty());

        var command = new UpdateConvocatoriaCommand(convId, tenantId, userId, "X", null);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void throwsConflictWhenConvocatoriaIsNotDraft() {
        Convocatoria active = draftConvocatoria();
        active.launch();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(active));

        var command = new UpdateConvocatoriaCommand(convId, tenantId, userId, "X", null);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    private Convocatoria draftConvocatoria() {
        return Convocatoria.builder().id(convId).tenantId(tenantId)
                .forms(List.of(ConvocatoriaForm.builder()
                        .convocatoriaId(convId).formId(UUID.randomUUID()).weight(100).build()))
                .name("Proceso Original")
                .type(FormType.CANDIDATES).status(ConvocatoriaStatus.DRAFT).build();
    }
}
