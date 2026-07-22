package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.Condition;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionOperator;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionalLogic;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionalLogicAction;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.LogicOperator;
import com.kodelabs.formflow.modules.forms.domain.model.config.TextConfig;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormClonerTest {

    @Mock private FormRepositoryPort formRepository;
    @Mock private FormSectionRepositoryPort sectionRepository;
    @Mock private FormQuestionRepositoryPort questionRepository;
    @InjectMocks private FormCloner cloner;

    private UUID tenantId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();

        when(formRepository.save(any())).thenAnswer(inv -> {
            Form f = inv.getArgument(0);
            f.setId(UUID.randomUUID());
            return f;
        });
        lenient().when(sectionRepository.save(any())).thenAnswer(inv -> {
            FormSection s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });
        lenient().when(questionRepository.save(any())).thenAnswer(inv -> {
            FormQuestion q = inv.getArgument(0);
            if (q.getId() == null) q.setId(UUID.randomUUID());
            return q;
        });
    }

    @Test
    void clonesFormMetadataWithGivenLineage() {
        Form origin = Form.builder().id(UUID.randomUUID()).tenantId(tenantId)
                .name("Original").description("Desc").type(FormType.CANDIDATES)
                .timeLimitSeconds(600).sections(List.of()).build();

        UUID rootId = UUID.randomUUID();
        Form result = cloner.clone(origin, userId, origin.getId(), rootId, 2);

        assertThat(result.getId()).isNotNull().isNotEqualTo(origin.getId());
        assertThat(result.getName()).isEqualTo("Original");
        assertThat(result.getDescription()).isEqualTo("Desc");
        assertThat(result.getType()).isEqualTo(FormType.CANDIDATES);
        assertThat(result.getTimeLimitSeconds()).isEqualTo(600);
        assertThat(result.getPreviousVersionId()).isEqualTo(origin.getId());
        assertThat(result.getRootFormId()).isEqualTo(rootId);
        assertThat(result.getVersion()).isEqualTo(2);
        assertThat(result.getCreatedBy()).isEqualTo(userId);
    }

    @Test
    void clonesSectionsAndQuestionsWithNewIdsAndCopiedFields() {
        UUID sectionId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        FormQuestion question = FormQuestion.builder().id(questionId).sectionId(sectionId)
                .title("Pregunta 1").description("Ayuda").type(QuestionType.TEXT).position(0)
                .required(true).categoryId(categoryId).timeLimitSeconds(30)
                .config(TextConfig.builder().maxLength(500).build()).build();
        FormSection section = FormSection.builder().id(sectionId).title("Sección 1")
                .description("Sec desc").position(0).timeLimitSeconds(120)
                .questions(List.of(question)).build();
        Form origin = Form.builder().id(UUID.randomUUID()).tenantId(tenantId)
                .name("F").type(FormType.CANDIDATES).sections(List.of(section)).build();

        Form result = cloner.clone(origin, userId, origin.getId(), origin.getId(), 2);

        ArgumentCaptor<FormSection> sectionCaptor = ArgumentCaptor.forClass(FormSection.class);
        verify(sectionRepository).save(sectionCaptor.capture());
        FormSection clonedSection = sectionCaptor.getValue();
        assertThat(clonedSection.getId()).isNotEqualTo(sectionId);
        assertThat(clonedSection.getFormId()).isEqualTo(result.getId());
        assertThat(clonedSection.getTitle()).isEqualTo("Sección 1");
        assertThat(clonedSection.getTimeLimitSeconds()).isEqualTo(120);

        ArgumentCaptor<FormQuestion> questionCaptor = ArgumentCaptor.forClass(FormQuestion.class);
        verify(questionRepository, times(1)).save(questionCaptor.capture());
        FormQuestion clonedQuestion = questionCaptor.getValue();
        assertThat(clonedQuestion.getId()).isNotEqualTo(questionId);
        assertThat(clonedQuestion.getSectionId()).isEqualTo(clonedSection.getId());
        assertThat(clonedQuestion.getFormId()).isEqualTo(result.getId());
        assertThat(clonedQuestion.getTitle()).isEqualTo("Pregunta 1");
        assertThat(clonedQuestion.isRequired()).isTrue();
        assertThat(clonedQuestion.getCategoryId()).isEqualTo(categoryId);
        assertThat(((TextConfig) clonedQuestion.getConfig()).getMaxLength()).isEqualTo(500);
    }

    @Test
    void remapsConditionalLogicSourceQuestionIdToTheClonedQuestion() {
        UUID sectionId = UUID.randomUUID();
        UUID triggerQuestionId = UUID.randomUUID();
        UUID dependentQuestionId = UUID.randomUUID();

        FormQuestion trigger = FormQuestion.builder().id(triggerQuestionId).sectionId(sectionId)
                .title("Disparador").type(QuestionType.SINGLE).position(0)
                .config(TextConfig.builder().build()).build();
        FormQuestion dependent = FormQuestion.builder().id(dependentQuestionId).sectionId(sectionId)
                .title("Dependiente").type(QuestionType.TEXT).position(1)
                .config(TextConfig.builder().build())
                .conditionalLogic(new ConditionalLogic(
                        ConditionalLogicAction.SHOW, LogicOperator.AND,
                        List.of(new Condition(triggerQuestionId, ConditionOperator.EQUALS, "si"))))
                .build();
        FormSection section = FormSection.builder().id(sectionId).title("S").position(0)
                .questions(List.of(trigger, dependent)).build();
        Form origin = Form.builder().id(UUID.randomUUID()).tenantId(tenantId)
                .name("F").type(FormType.CANDIDATES).sections(List.of(section)).build();

        cloner.clone(origin, userId, origin.getId(), origin.getId(), 2);

        ArgumentCaptor<FormQuestion> questionCaptor = ArgumentCaptor.forClass(FormQuestion.class);
        verify(questionRepository, times(3)).save(questionCaptor.capture());
        List<FormQuestion> savedCalls = questionCaptor.getAllValues();

        // First two calls are the initial inserts (no conditional logic yet); the third is the
        // update that attaches the remapped conditional logic to the cloned "dependent" question.
        FormQuestion updateCall = savedCalls.get(2);
        UUID clonedTriggerId = savedCalls.get(0).getId();
        assertThat(updateCall.getConditionalLogic()).isNotNull();
        assertThat(updateCall.getConditionalLogic().conditions()).hasSize(1);
        assertThat(updateCall.getConditionalLogic().conditions().get(0).sourceQuestionId())
                .isEqualTo(clonedTriggerId)
                .isNotEqualTo(triggerQuestionId);
    }

    @Test
    void clonesFormWithoutSectionsOrQuestionsWithoutError() {
        Form origin = Form.builder().id(UUID.randomUUID()).tenantId(tenantId)
                .name("F").type(FormType.REGISTRATION).sections(List.of()).build();

        Form result = cloner.clone(origin, userId, null, null, 1);

        assertThat(result).isNotNull();
        assertThat(result.getPreviousVersionId()).isNull();
        assertThat(result.getRootFormId()).isNull();
        verify(sectionRepository, times(0)).save(any());
        verify(questionRepository, times(0)).save(any());
    }
}
