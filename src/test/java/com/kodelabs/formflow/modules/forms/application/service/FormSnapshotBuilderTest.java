package com.kodelabs.formflow.modules.forms.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.AnswerOption;
import com.kodelabs.formflow.modules.forms.domain.model.config.SingleConfig;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.FormSnapshot;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormSnapshotBuilderTest {

    @Mock private FormLoader formLoader;
    @Mock private FormSectionRepositoryPort sectionRepository;
    @Mock private FormQuestionRepositoryPort questionRepository;
    @Spy  private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks private FormSnapshotBuilder builder;

    private UUID formId;
    private UUID tenantId;
    private UUID sectionId;
    private Form form;
    private FormSection section;

    @BeforeEach
    void setUp() {
        formId    = UUID.randomUUID();
        tenantId  = UUID.randomUUID();
        sectionId = UUID.randomUUID();
        form = Form.builder().id(formId).tenantId(tenantId).name("Evaluación").type(FormType.CANDIDATES).version(3).build();
        section = FormSection.builder().id(sectionId).formId(formId).title("Sección 1").position(0).build();
    }

    @Test
    void snapshotCapturesFormMetadataAndVersion() {
        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(sectionRepository.findActiveByFormIdAndTenantId(formId, tenantId)).thenReturn(List.of());

        FormSnapshot snapshot = builder.build(formId, tenantId);

        assertThat(snapshot.formId()).isEqualTo(formId);
        assertThat(snapshot.formName()).isEqualTo("Evaluación");
        assertThat(snapshot.formType()).isEqualTo("CANDIDATES");
        assertThat(snapshot.formVersion()).isEqualTo(3);
        assertThat(snapshot.capturedAt()).isNotNull();
    }

    @Test
    void snapshotIncludesSectionsWithQuestionsInOrder() {
        UUID q1Id = UUID.randomUUID();
        UUID q2Id = UUID.randomUUID();
        FormQuestion q1 = FormQuestion.builder().id(q1Id).sectionId(sectionId).title("P1")
                .type(QuestionType.SINGLE).position(0).required(true)
                .config(SingleConfig.builder()
                        .options(List.of(AnswerOption.builder().label("Sí").score(10).build()))
                        .build())
                .build();
        FormQuestion q2 = FormQuestion.builder().id(q2Id).sectionId(sectionId).title("P2")
                .type(QuestionType.TEXT).position(1).build();

        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(sectionRepository.findActiveByFormIdAndTenantId(formId, tenantId)).thenReturn(List.of(section));
        when(questionRepository.findAllActiveBySectionIds(List.of(sectionId)))
                .thenReturn(Map.of(sectionId, List.of(q1, q2)));

        FormSnapshot snapshot = builder.build(formId, tenantId);

        assertThat(snapshot.sections()).hasSize(1);
        assertThat(snapshot.sections().get(0).id()).isEqualTo(sectionId);
        assertThat(snapshot.sections().get(0).questions()).hasSize(2);
        assertThat(snapshot.sections().get(0).questions().get(0).id()).isEqualTo(q1Id);
        assertThat(snapshot.sections().get(0).questions().get(0).required()).isTrue();
    }

    @Test
    void snapshotSerializesQuestionConfigAsMap() {
        FormQuestion question = FormQuestion.builder().id(UUID.randomUUID()).sectionId(sectionId).title("Q")
                .type(QuestionType.SINGLE).position(0)
                .config(SingleConfig.builder()
                        .options(List.of(AnswerOption.builder().label("Opción A").score(5).build()))
                        .build())
                .build();

        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(sectionRepository.findActiveByFormIdAndTenantId(formId, tenantId)).thenReturn(List.of(section));
        when(questionRepository.findAllActiveBySectionIds(List.of(sectionId)))
                .thenReturn(Map.of(sectionId, List.of(question)));

        FormSnapshot snapshot = builder.build(formId, tenantId);

        Map<String, Object> config = snapshot.sections().get(0).questions().get(0).config();
        assertThat(config).isNotNull().containsKey("options");
    }

    @Test
    void snapshotIsEmptyWhenFormHasNoSections() {
        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(sectionRepository.findActiveByFormIdAndTenantId(formId, tenantId)).thenReturn(List.of());

        FormSnapshot snapshot = builder.build(formId, tenantId);

        assertThat(snapshot.sections()).isEmpty();
    }

    @Test
    void throwsNotFoundWhenFormDoesNotBelongToTenant() {
        when(formLoader.loadOrThrow(formId, tenantId)).thenThrow(new BusinessException("error.form.not_found", HttpStatus.NOT_FOUND, formId));

        assertThatThrownBy(() -> builder.build(formId, tenantId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
