package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.usecase.form.CreateFormService;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.CreateFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormSummaryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateFormServiceTest {

    @Mock private FormRepositoryPort formRepository;
    @InjectMocks private CreateFormService service;

    private UUID tenantId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void savesFormWithCommandFieldsAndReturnsZeroSections() {
        Form saved = Form.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .name("Evaluacion de candidatos")
                .type(FormType.CANDIDATES)
                .version(1)
                .build();
        when(formRepository.save(any())).thenReturn(saved);

        FormSummaryResult result = service.execute(new CreateFormCommand(
                tenantId, userId, "Evaluacion de candidatos", null, FormType.CANDIDATES, null));

        assertThat(result.name()).isEqualTo("Evaluacion de candidatos");
        assertThat(result.type()).isEqualTo(FormType.CANDIDATES);
        assertThat(result.sectionCount()).isZero();
    }

    @Test
    void setsCreatedByAndUpdatedByFromCommand() {
        when(formRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute(new CreateFormCommand(tenantId, userId, "Form", null, FormType.REGISTRATION, null));

        ArgumentCaptor<Form> captor = ArgumentCaptor.forClass(Form.class);
        verify(formRepository).save(captor.capture());
        assertThat(captor.getValue().getCreatedBy()).isEqualTo(userId);
        assertThat(captor.getValue().getUpdatedBy()).isEqualTo(userId);
    }
}
