package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.usecase.form.GetFormService;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetFormQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormDetailResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetFormServiceTest {

    @Mock private FormRepositoryPort formRepository;
    @InjectMocks private GetFormService service;

    @Test
    void returnsFormWithSectionsWhenFound() {
        UUID formId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        FormSection section = FormSection.builder().id(UUID.randomUUID()).formId(formId).title("S1").position(0).build();
        Form form = Form.builder().id(formId).tenantId(tenantId).name("F").type(FormType.CANDIDATES)
                .sections(List.of(section)).build();
        when(formRepository.findByIdAndTenantIdWithSections(formId, tenantId)).thenReturn(Optional.of(form));

        FormDetailResult result = service.execute(new GetFormQuery(formId, tenantId));

        assertThat(result.form().getId()).isEqualTo(formId);
        assertThat(result.form().getSections()).hasSize(1);
    }

    @Test
    void throwsNotFoundWhenFormDoesNotExist() {
        UUID formId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        when(formRepository.findByIdAndTenantIdWithSections(formId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new GetFormQuery(formId, tenantId)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.form.not_found")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
