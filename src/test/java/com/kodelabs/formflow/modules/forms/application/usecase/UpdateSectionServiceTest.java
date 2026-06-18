package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateSectionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SectionResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
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
class UpdateSectionServiceTest {

    @Mock private FormSectionRepositoryPort sectionRepository;
    @InjectMocks private UpdateSectionService service;

    @Test
    void updatesTitleDescriptionAndTimeLimitSeconds() {
        UUID sectionId = UUID.randomUUID();
        UUID formId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        FormSection section = FormSection.builder().id(sectionId).formId(formId).title("Viejo").position(0).build();
        when(sectionRepository.findByIdAndFormIdAndTenantId(sectionId, formId, tenantId))
                .thenReturn(Optional.of(section));
        when(sectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SectionResult result = service.execute(new UpdateSectionCommand(sectionId, formId, tenantId, "Nuevo", "Desc", 120));

        assertThat(result.title()).isEqualTo("Nuevo");
        assertThat(result.description()).isEqualTo("Desc");
        assertThat(result.timeLimitSeconds()).isEqualTo(120);
    }

    @Test
    void clearsTimeLimitSecondsWhenNullIsProvided() {
        UUID sectionId = UUID.randomUUID();
        UUID formId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        FormSection section = FormSection.builder().id(sectionId).formId(formId).title("S")
                .position(0).timeLimitSeconds(60).build();
        when(sectionRepository.findByIdAndFormIdAndTenantId(sectionId, formId, tenantId))
                .thenReturn(Optional.of(section));
        when(sectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SectionResult result = service.execute(new UpdateSectionCommand(sectionId, formId, tenantId, "S", null, null));

        assertThat(result.timeLimitSeconds()).isNull();
    }

    @Test
    void throwsNotFoundWhenSectionDoesNotExist() {
        UUID sectionId = UUID.randomUUID();
        UUID formId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        when(sectionRepository.findByIdAndFormIdAndTenantId(sectionId, formId, tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(
                new UpdateSectionCommand(sectionId, formId, tenantId, "T", null, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.section.not_found")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
