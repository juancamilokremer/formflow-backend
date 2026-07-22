package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.service.FormCloner;
import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.application.usecase.form.DuplicateFormService;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.DuplicateFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormSummaryResult;
import com.kodelabs.formflow.shared.i18n.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DuplicateFormServiceTest {

    @Mock private FormLoader formLoader;
    @Mock private FormCloner formCloner;
    @Mock private Messages messages;
    @InjectMocks private DuplicateFormService service;

    private UUID formId;
    private UUID tenantId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        formId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @ParameterizedTest
    @EnumSource(FormStatus.class)
    void duplicatesRegardlessOfSourceStatus(FormStatus status) {
        Form origin = Form.builder().id(formId).tenantId(tenantId).name("Evaluación técnica")
                .type(FormType.CANDIDATES).status(status).sections(List.of()).build();
        when(formLoader.loadWithSectionsOrThrow(formId, tenantId)).thenReturn(origin);
        when(messages.get("form.duplicate_name_suffix", "Evaluación técnica"))
                .thenReturn("Evaluación técnica (copia)");
        Form duplicate = Form.builder().id(UUID.randomUUID()).tenantId(tenantId)
                .name("Evaluación técnica (copia)").type(FormType.CANDIDATES)
                .status(FormStatus.DRAFT).version(1).build();
        when(formCloner.clone(eq(origin), eq(userId), isNull(), isNull(), eq(1), eq("Evaluación técnica (copia)")))
                .thenReturn(duplicate);

        FormSummaryResult result = service.execute(new DuplicateFormCommand(formId, tenantId, userId));

        assertThat(result.id()).isEqualTo(duplicate.getId());
        assertThat(result.name()).isEqualTo("Evaluación técnica (copia)");
        assertThat(result.status()).isEqualTo(FormStatus.DRAFT);
        assertThat(result.version()).isEqualTo(1);
    }
}
