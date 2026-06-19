package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.usecase.form.DeleteFormService;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.DeleteFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteFormServiceTest {

    @Mock private FormRepositoryPort formRepository;
    @InjectMocks private DeleteFormService service;

    @Test
    void softDeletesSetsDeletedAt() {
        UUID formId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Form form = Form.builder().id(formId).tenantId(tenantId).name("F").type(FormType.CANDIDATES).build();
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.of(form));
        when(formRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute(new DeleteFormCommand(formId, tenantId));

        ArgumentCaptor<Form> captor = ArgumentCaptor.forClass(Form.class);
        verify(formRepository).save(captor.capture());
        assertThat(captor.getValue().isDeleted()).isTrue();
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }

    @Test
    void throwsNotFoundWhenFormDoesNotExist() {
        UUID formId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new DeleteFormCommand(formId, tenantId)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.form.not_found")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
