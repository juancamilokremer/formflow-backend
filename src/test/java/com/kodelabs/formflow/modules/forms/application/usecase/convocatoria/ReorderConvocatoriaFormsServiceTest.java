package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ReorderConvocatoriaFormsCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaFormResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaFormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
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
class ReorderConvocatoriaFormsServiceTest {

    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private ConvocatoriaFormRepositoryPort convocatoriaFormRepository;
    @InjectMocks private ReorderConvocatoriaFormsService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId   = UUID.randomUUID();
    private final UUID convId   = UUID.randomUUID();

    private UUID f1Id;
    private UUID f2Id;
    private UUID f3Id;
    private ConvocatoriaForm f1;
    private ConvocatoriaForm f2;
    private ConvocatoriaForm f3;

    @BeforeEach
    void setUp() {
        f1Id = UUID.randomUUID();
        f2Id = UUID.randomUUID();
        f3Id = UUID.randomUUID();
        f1 = ConvocatoriaForm.builder().id(f1Id).convocatoriaId(convId).formId(UUID.randomUUID()).weight(34).position(0).build();
        f2 = ConvocatoriaForm.builder().id(f2Id).convocatoriaId(convId).formId(UUID.randomUUID()).weight(33).position(1).build();
        f3 = ConvocatoriaForm.builder().id(f3Id).convocatoriaId(convId).formId(UUID.randomUUID()).weight(33).position(2).build();
    }

    @Test
    void assignsNewPositionsInRequestedOrder() {
        Convocatoria draft = draftConvocatoria(List.of(f1, f2, f3));
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draft));
        when(convocatoriaFormRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        List<ConvocatoriaFormResult> results = service.execute(
                new ReorderConvocatoriaFormsCommand(convId, tenantId, userId, List.of(f3Id, f1Id, f2Id)));

        assertThat(results).hasSize(3);
        assertThat(results.get(0).id()).isEqualTo(f3Id);
        assertThat(results.get(0).position()).isZero();
        assertThat(results.get(1).id()).isEqualTo(f1Id);
        assertThat(results.get(1).position()).isEqualTo(1);
        assertThat(results.get(2).id()).isEqualTo(f2Id);
        assertThat(results.get(2).position()).isEqualTo(2);
    }

    @Test
    void throwsBadRequestWhenOrderedIdsMissAForm() {
        Convocatoria draft = draftConvocatoria(List.of(f1, f2, f3));
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draft));

        var command = new ReorderConvocatoriaFormsCommand(convId, tenantId, userId, List.of(f1Id, f2Id));
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.convocatoria.form_reorder_invalid")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void throwsBadRequestWhenOrderedIdsContainUnknownId() {
        Convocatoria draft = draftConvocatoria(List.of(f1, f2, f3));
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(draft));

        var command = new ReorderConvocatoriaFormsCommand(
                convId, tenantId, userId, List.of(f1Id, f2Id, UUID.randomUUID()));
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.convocatoria.form_reorder_invalid");
    }

    @Test
    void throwsNotFoundWhenConvocatoriaDoesNotExist() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.empty());

        var command = new ReorderConvocatoriaFormsCommand(convId, tenantId, userId, List.of());
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private Convocatoria draftConvocatoria(List<ConvocatoriaForm> forms) {
        return Convocatoria.builder().id(convId).tenantId(tenantId)
                .forms(forms).name("Test").status(ConvocatoriaStatus.DRAFT).build();
    }
}
