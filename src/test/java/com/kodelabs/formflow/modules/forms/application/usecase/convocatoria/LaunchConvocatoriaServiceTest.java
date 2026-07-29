package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.ConvocatoriaEmailSender;
import com.kodelabs.formflow.modules.forms.application.service.ConvocatoriaWeightValidator;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.LaunchConvocatoriaCommand;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LaunchConvocatoriaServiceTest {

    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private CandidateRepositoryPort candidateRepository;
    @Mock private FormRepositoryPort formRepository;
    @Mock private ConvocatoriaEmailSender emailSender;
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
        when(formRepository.findByIdAndTenantId(draft.getFormId(), tenantId)).thenReturn(Optional.of(activeForm(draft.getFormId())));
        when(convocatoriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(candidateRepository.findAllByConvocatoriaId(convId)).thenReturn(List.of());

        var result = service.execute(new LaunchConvocatoriaCommand(convId, tenantId, userId));

        assertThat(result.status()).isEqualTo(ConvocatoriaStatus.ACTIVE.name());
    }

    @Test
    void publishesTheAttachedFormWhenStillDraft() {
        Convocatoria draft = draftConvocatoria();
        Form draftForm = Form.builder().id(draft.getFormId()).tenantId(tenantId).status(FormStatus.DRAFT).build();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draft));
        when(candidateRepository.countByConvocatoriaId(convId)).thenReturn(1L);
        when(formRepository.findByIdAndTenantId(draft.getFormId(), tenantId)).thenReturn(Optional.of(draftForm));
        when(convocatoriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(candidateRepository.findAllByConvocatoriaId(convId)).thenReturn(List.of());
        ArgumentCaptor<Form> formCaptor = ArgumentCaptor.forClass(Form.class);

        service.execute(new LaunchConvocatoriaCommand(convId, tenantId, userId));

        verify(formRepository).save(formCaptor.capture());
        assertThat(formCaptor.getValue().getStatus()).isEqualTo(FormStatus.ACTIVE);
        assertThat(formCaptor.getValue().getUpdatedBy()).isEqualTo(userId);
    }

    @Test
    void doesNotResaveAnAlreadyActiveForm() {
        Convocatoria draft = draftConvocatoria();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draft));
        when(candidateRepository.countByConvocatoriaId(convId)).thenReturn(1L);
        when(formRepository.findByIdAndTenantId(draft.getFormId(), tenantId)).thenReturn(Optional.of(activeForm(draft.getFormId())));
        when(convocatoriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(candidateRepository.findAllByConvocatoriaId(convId)).thenReturn(List.of());

        service.execute(new LaunchConvocatoriaCommand(convId, tenantId, userId));

        verify(formRepository, never()).save(any());
    }

    @Test
    void sendsInvitationEmailToEachCandidate() {
        Candidate c1 = Candidate.builder().id(UUID.randomUUID()).convocatoriaId(convId)
                .tenantId(tenantId).name("Ana").email("ana@test.com")
                .status(CandidateStatus.INVITED).token(UUID.randomUUID()).build();
        Candidate c2 = Candidate.builder().id(UUID.randomUUID()).convocatoriaId(convId)
                .tenantId(tenantId).name("Luis").email("luis@test.com")
                .status(CandidateStatus.INVITED).token(UUID.randomUUID()).build();
        Convocatoria draft = draftConvocatoria();

        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draft));
        when(candidateRepository.countByConvocatoriaId(convId)).thenReturn(2L);
        when(formRepository.findByIdAndTenantId(draft.getFormId(), tenantId)).thenReturn(Optional.of(activeForm(draft.getFormId())));
        when(convocatoriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(candidateRepository.findAllByConvocatoriaId(convId)).thenReturn(List.of(c1, c2));

        service.execute(new LaunchConvocatoriaCommand(convId, tenantId, userId));

        verify(emailSender, times(2)).sendInvitation(any(Candidate.class), any(Convocatoria.class));
    }

    @Test
    void throwsConflictWhenAlreadyActive() {
        Convocatoria active = draftConvocatoria();
        active.launch();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(active));

        var command = new LaunchConvocatoriaCommand(convId, tenantId, userId);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void throwsBadRequestWhenNoCandidates() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draftConvocatoria()));
        when(candidateRepository.countByConvocatoriaId(convId)).thenReturn(0L);

        var command = new LaunchConvocatoriaCommand(convId, tenantId, userId);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void throwsBadRequestWhenNoFormAttached() {
        Convocatoria draft = draftConvocatoriaWithoutForm();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draft));

        var command = new LaunchConvocatoriaCommand(convId, tenantId, userId);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void throwsBadRequestWhenFormWeightsDoNotSumTo100() {
        Convocatoria draft = Convocatoria.builder().id(convId).tenantId(tenantId)
                .forms(List.of(
                        ConvocatoriaForm.builder().convocatoriaId(convId).formId(UUID.randomUUID()).weight(60).build(),
                        ConvocatoriaForm.builder().convocatoriaId(convId).formId(UUID.randomUUID()).weight(30).build()))
                .name("Test").status(ConvocatoriaStatus.DRAFT).build();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draft));

        var command = new LaunchConvocatoriaCommand(convId, tenantId, userId);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void publishesEveryAttachedFormWhenStillDraft() {
        UUID formId1 = UUID.randomUUID();
        UUID formId2 = UUID.randomUUID();
        Convocatoria draft = Convocatoria.builder().id(convId).tenantId(tenantId)
                .forms(List.of(
                        ConvocatoriaForm.builder().convocatoriaId(convId).formId(formId1).weight(60).build(),
                        ConvocatoriaForm.builder().convocatoriaId(convId).formId(formId2).weight(40).build()))
                .name("Test").status(ConvocatoriaStatus.DRAFT).build();
        Form draftForm1 = Form.builder().id(formId1).tenantId(tenantId).status(FormStatus.DRAFT).build();
        Form draftForm2 = Form.builder().id(formId2).tenantId(tenantId).status(FormStatus.DRAFT).build();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draft));
        when(candidateRepository.countByConvocatoriaId(convId)).thenReturn(1L);
        when(formRepository.findByIdAndTenantId(formId1, tenantId)).thenReturn(Optional.of(draftForm1));
        when(formRepository.findByIdAndTenantId(formId2, tenantId)).thenReturn(Optional.of(draftForm2));
        when(convocatoriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(candidateRepository.findAllByConvocatoriaId(convId)).thenReturn(List.of());

        service.execute(new LaunchConvocatoriaCommand(convId, tenantId, userId));

        verify(formRepository, times(2)).save(any());
    }

    @Test
    void throwsNotFoundWhenConvocatoriaDoesNotExist() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.empty());

        var command = new LaunchConvocatoriaCommand(convId, tenantId, userId);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private Convocatoria draftConvocatoria() {
        return Convocatoria.builder().id(convId).tenantId(tenantId)
                .forms(List.of(ConvocatoriaForm.builder()
                        .convocatoriaId(convId).formId(UUID.randomUUID()).weight(100).build()))
                .name("Test").status(ConvocatoriaStatus.DRAFT).build();
    }

    private Form activeForm(UUID formId) {
        return Form.builder().id(formId).tenantId(tenantId).status(FormStatus.ACTIVE).build();
    }

    private Convocatoria draftConvocatoriaWithoutForm() {
        return Convocatoria.builder().id(convId).tenantId(tenantId)
                .name("Test").status(ConvocatoriaStatus.DRAFT).build();
    }
}
