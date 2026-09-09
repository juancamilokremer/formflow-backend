package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.QuestionStatsComputer;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetConvocatoriaQuestionStatsQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaQuestionStatsResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormQuestionStatsResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionStatsResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetConvocatoriaQuestionStatsServiceTest {

    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private FormRepositoryPort formRepository;
    @Mock private FormResponseRepositoryPort responseRepository;
    @Mock private QuestionStatsComputer questionStatsComputer;
    @InjectMocks private GetConvocatoriaQuestionStatsService service;

    private final UUID convId = UUID.randomUUID();
    private final UUID tenantId = UUID.randomUUID();

    @Test
    void aggregatesQuestionStatsPerFormScopedToTheConvocatoriaResponses() {
        UUID form1Id = UUID.randomUUID();
        UUID form2Id = UUID.randomUUID();
        Convocatoria convocatoria = convocatoriaWithForms(form1Id, form2Id);
        Form form1 = formWithId(form1Id, "Evaluación técnica");
        Form form2 = formWithId(form2Id, "Evaluación de cultura");

        FormResponse responseForForm1 = FormResponse.builder().id(UUID.randomUUID()).formId(form1Id).build();

        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(convocatoria));
        when(responseRepository.findAllByConvocatoriaIdAndTenantId(convId, tenantId, null, null))
                .thenReturn(List.of(responseForForm1));
        when(formRepository.findByIdAndTenantIdWithSections(form1Id, tenantId)).thenReturn(Optional.of(form1));
        when(formRepository.findByIdAndTenantIdWithSections(form2Id, tenantId)).thenReturn(Optional.of(form2));

        QuestionStatsResult form1Stats = new QuestionStatsResult(
                UUID.randomUUID(), "Pregunta", "single", 1, 1, List.of(), null, null, null, null, List.of());
        when(questionStatsComputer.compute(eq(form1), eq(1), any())).thenReturn(List.of(form1Stats));
        when(questionStatsComputer.compute(eq(form2), eq(0), any())).thenReturn(List.of());

        ConvocatoriaQuestionStatsResult result =
                service.execute(new GetConvocatoriaQuestionStatsQuery(convId, tenantId, null, null));

        assertThat(result.convocatoriaId()).isEqualTo(convId);
        assertThat(result.forms()).hasSize(2);

        FormQuestionStatsResult form1Result = result.forms().stream()
                .filter(f -> f.formId().equals(form1Id)).findFirst().orElseThrow();
        assertThat(form1Result.totalResponses()).isEqualTo(1);
        assertThat(form1Result.questions()).containsExactly(form1Stats);

        FormQuestionStatsResult form2Result = result.forms().stream()
                .filter(f -> f.formId().equals(form2Id)).findFirst().orElseThrow();
        assertThat(form2Result.totalResponses()).isZero();
        assertThat(form2Result.questions()).isEmpty();
    }

    @Test
    void returnsEmptyFormsListWhenConvocatoriaHasNoFormsAttached() {
        Convocatoria convocatoria = Convocatoria.builder()
                .id(convId).tenantId(tenantId).name("Sin formularios")
                .status(ConvocatoriaStatus.DRAFT)
                .build();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(convocatoria));
        when(responseRepository.findAllByConvocatoriaIdAndTenantId(convId, tenantId, null, null))
                .thenReturn(List.of());

        ConvocatoriaQuestionStatsResult result =
                service.execute(new GetConvocatoriaQuestionStatsQuery(convId, tenantId, null, null));

        assertThat(result.forms()).isEmpty();
    }

    @Test
    void throwsNotFoundWhenConvocatoriaDoesNotBelongToTenant() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new GetConvocatoriaQuestionStatsQuery(convId, tenantId, null, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private Convocatoria convocatoriaWithForms(UUID form1Id, UUID form2Id) {
        return Convocatoria.builder()
                .id(convId).tenantId(tenantId).name("Analista de RRHH")
                .status(ConvocatoriaStatus.ACTIVE)
                .forms(List.of(
                        ConvocatoriaForm.builder().id(UUID.randomUUID()).formId(form1Id).weight(60).position(0).build(),
                        ConvocatoriaForm.builder().id(UUID.randomUUID()).formId(form2Id).weight(40).position(1).build()))
                .build();
    }

    private Form formWithId(UUID formId, String name) {
        return Form.builder()
                .id(formId).tenantId(tenantId).name(name)
                .type(FormType.CANDIDATES).version(1)
                .sections(List.of())
                .build();
    }
}
