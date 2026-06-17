package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ReorderSectionsCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SectionResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReorderSectionsServiceTest {

    @Mock private FormRepositoryPort formRepository;
    @Mock private FormSectionRepositoryPort sectionRepository;
    @InjectMocks private ReorderSectionsService service;

    private UUID formId;
    private UUID tenantId;
    private UUID userId;
    private Form form;
    private UUID s1Id;
    private UUID s2Id;
    private UUID s3Id;
    private FormSection s1;
    private FormSection s2;
    private FormSection s3;

    @BeforeEach
    void setUp() {
        formId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        form = Form.builder().id(formId).tenantId(tenantId).name("F").type(FormType.CANDIDATES).version(1).build();
        s1Id = UUID.randomUUID();
        s2Id = UUID.randomUUID();
        s3Id = UUID.randomUUID();
        s1 = FormSection.builder().id(s1Id).formId(formId).title("S1").position(0).build();
        s2 = FormSection.builder().id(s2Id).formId(formId).title("S2").position(1).build();
        s3 = FormSection.builder().id(s3Id).formId(formId).title("S3").position(2).build();
    }

    @Test
    void assignsNewPositionsInRequestedOrderAndIncrementsVersion() {
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.of(form));
        when(sectionRepository.findActiveByFormIdAndTenantId(formId, tenantId)).thenReturn(List.of(s1, s2, s3));
        when(formRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<SectionResult> results = service.execute(
                new ReorderSectionsCommand(formId, tenantId, userId, List.of(s3Id, s1Id, s2Id)));

        assertThat(results).hasSize(3);
        assertThat(results.get(0).title()).isEqualTo("S3");
        assertThat(results.get(0).position()).isZero();
        assertThat(results.get(1).title()).isEqualTo("S1");
        assertThat(results.get(1).position()).isEqualTo(1);
        assertThat(results.get(2).title()).isEqualTo("S2");
        assertThat(results.get(2).position()).isEqualTo(2);

        ArgumentCaptor<Form> formCaptor = ArgumentCaptor.forClass(Form.class);
        verify(formRepository).save(formCaptor.capture());
        assertThat(formCaptor.getValue().getVersion()).isEqualTo(2);
    }

    @Test
    void throwsBadRequestWhenOrderedIdsMissASection() {
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.of(form));
        when(sectionRepository.findActiveByFormIdAndTenantId(formId, tenantId)).thenReturn(List.of(s1, s2, s3));

        assertThatThrownBy(() -> service.execute(
                new ReorderSectionsCommand(formId, tenantId, userId, List.of(s1Id, s2Id))))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.section.reorder_invalid")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void throwsBadRequestWhenOrderedIdsContainUnknownId() {
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.of(form));
        when(sectionRepository.findActiveByFormIdAndTenantId(formId, tenantId)).thenReturn(List.of(s1, s2, s3));

        UUID unknownId = UUID.randomUUID();
        assertThatThrownBy(() -> service.execute(
                new ReorderSectionsCommand(formId, tenantId, userId, List.of(s1Id, s2Id, unknownId))))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.section.reorder_invalid");
    }

    @Test
    void throwsNotFoundWhenFormDoesNotBelongToTenant() {
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(
                new ReorderSectionsCommand(formId, tenantId, userId, List.of(s1Id))))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.form.not_found")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
