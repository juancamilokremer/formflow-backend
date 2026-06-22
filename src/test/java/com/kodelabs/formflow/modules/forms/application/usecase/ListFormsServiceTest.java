package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.usecase.form.ListFormsService;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ListFormsQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormSummaryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListFormsServiceTest {

    @Mock private FormRepositoryPort formRepository;
    @Mock private FormSectionRepositoryPort sectionRepository;
    @Mock private FormResponseRepositoryPort responseRepository;
    @InjectMocks private ListFormsService service;

    @Test
    void returnsEmptyListWithoutQueryingSectionsWhenNoForms() {
        UUID tenantId = UUID.randomUUID();
        when(formRepository.findAllByTenantId(tenantId)).thenReturn(List.of());

        List<FormSummaryResult> results = service.execute(new ListFormsQuery(tenantId));

        assertThat(results).isEmpty();
        verify(sectionRepository, never()).countAllActiveByFormIds(any());
        verify(responseRepository, never()).countByFormIds(any());
    }

    @Test
    void returnsSectionCountPerFormInSingleBatchQuery() {
        UUID tenantId = UUID.randomUUID();
        UUID formId1 = UUID.randomUUID();
        UUID formId2 = UUID.randomUUID();
        Form form1 = Form.builder().id(formId1).tenantId(tenantId).name("F1").type(FormType.CANDIDATES).build();
        Form form2 = Form.builder().id(formId2).tenantId(tenantId).name("F2").type(FormType.REGISTRATION).build();
        when(formRepository.findAllByTenantId(tenantId)).thenReturn(List.of(form1, form2));
        when(sectionRepository.countAllActiveByFormIds(List.of(formId1, formId2)))
                .thenReturn(Map.of(formId1, 3, formId2, 0));
        when(responseRepository.countByFormIds(any())).thenReturn(Map.of(formId1, 5));
        when(responseRepository.lastResponseAtByFormIds(any())).thenReturn(Map.of());

        List<FormSummaryResult> results = service.execute(new ListFormsQuery(tenantId));

        assertThat(results).hasSize(2);
        assertThat(results.stream().filter(r -> r.id().equals(formId1)).findFirst())
                .get().extracting(FormSummaryResult::sectionCount).isEqualTo(3);
        assertThat(results.stream().filter(r -> r.id().equals(formId1)).findFirst())
                .get().extracting(FormSummaryResult::responseCount).isEqualTo(5);
        assertThat(results.stream().filter(r -> r.id().equals(formId2)).findFirst())
                .get().extracting(FormSummaryResult::sectionCount).isEqualTo(0);
    }

    @Test
    void defaultsToZeroWhenFormHasNoCountInBatchResult() {
        UUID tenantId = UUID.randomUUID();
        UUID formId = UUID.randomUUID();
        Form form = Form.builder().id(formId).tenantId(tenantId).name("F").type(FormType.DIAGNOSTIC).build();
        when(formRepository.findAllByTenantId(tenantId)).thenReturn(List.of(form));
        when(sectionRepository.countAllActiveByFormIds(any())).thenReturn(Map.of());
        when(responseRepository.countByFormIds(any())).thenReturn(Map.of());
        when(responseRepository.lastResponseAtByFormIds(any())).thenReturn(Map.of());

        List<FormSummaryResult> results = service.execute(new ListFormsQuery(tenantId));

        assertThat(results.get(0).sectionCount()).isZero();
        assertThat(results.get(0).responseCount()).isZero();
        assertThat(results.get(0).lastResponseAt()).isNull();
    }
}
