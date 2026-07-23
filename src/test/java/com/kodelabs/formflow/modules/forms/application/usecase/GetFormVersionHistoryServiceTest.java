package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.application.usecase.form.GetFormVersionHistoryService;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetFormVersionHistoryQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormVersionResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetFormVersionHistoryServiceTest {

    @Mock private FormLoader formLoader;
    @Mock private FormRepositoryPort formRepository;
    @InjectMocks private GetFormVersionHistoryService service;

    private UUID formId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        formId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
    }

    @Test
    void queriesFamilyBySelfWhenFormHasNoLineage() {
        Form origin = Form.builder().id(formId).tenantId(tenantId)
                .type(FormType.CANDIDATES).status(FormStatus.ARCHIVED).version(1).build();
        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(origin);
        when(formRepository.findFamilyByRootId(formId, tenantId)).thenReturn(List.of(origin));

        service.execute(new GetFormVersionHistoryQuery(formId, tenantId));

        verify(formRepository).findFamilyByRootId(formId, tenantId);
    }

    @Test
    void queriesFamilyByInheritedRootWhenFormIsAlreadyAVersion() {
        UUID rootId = UUID.randomUUID();
        Form form = Form.builder().id(formId).tenantId(tenantId).rootFormId(rootId)
                .type(FormType.DIAGNOSTIC).status(FormStatus.DRAFT).version(2).build();
        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(formRepository.findFamilyByRootId(rootId, tenantId)).thenReturn(List.of(form));

        service.execute(new GetFormVersionHistoryQuery(formId, tenantId));

        verify(formRepository).findFamilyByRootId(rootId, tenantId);
    }

    @Test
    void mapsTheFullFamilyInOrder() {
        Form v1 = Form.builder().id(formId).tenantId(tenantId).type(FormType.CANDIDATES)
                .status(FormStatus.ARCHIVED).version(1).build();
        Form v2 = Form.builder().id(UUID.randomUUID()).tenantId(tenantId).rootFormId(formId)
                .type(FormType.CANDIDATES).status(FormStatus.ARCHIVED).version(2).build();
        Form v3 = Form.builder().id(UUID.randomUUID()).tenantId(tenantId).rootFormId(formId)
                .type(FormType.CANDIDATES).status(FormStatus.DRAFT).version(3).build();
        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(v1);
        when(formRepository.findFamilyByRootId(formId, tenantId)).thenReturn(List.of(v1, v2, v3));

        List<FormVersionResult> result = service.execute(new GetFormVersionHistoryQuery(formId, tenantId));

        assertThat(result).hasSize(3);
        assertThat(result.stream().map(FormVersionResult::version)).containsExactly(1, 2, 3);
        assertThat(result.get(0).id()).isEqualTo(v1.getId());
        assertThat(result.get(2).status()).isEqualTo(FormStatus.DRAFT);
    }
}
