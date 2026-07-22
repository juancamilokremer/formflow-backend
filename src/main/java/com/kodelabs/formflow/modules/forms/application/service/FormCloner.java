package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.Condition;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionalLogic;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Clones a Form's full structure (sections, questions, conditional logic) into a new Form.
 * Shared by "generate new version" (with lineage) and "duplicate" (without lineage) —
 * the only difference is whether previousVersionId/rootFormId are populated.
 *
 * Conditional logic references other questions of the same form by id (sourceQuestionId),
 * so cloned questions get new ids and those references must be remapped — otherwise the
 * cloned logic would point to ids that don't exist in the new form.
 */
@Component
@RequiredArgsConstructor
public class FormCloner {

    private final FormRepositoryPort formRepository;
    private final FormSectionRepositoryPort sectionRepository;
    private final FormQuestionRepositoryPort questionRepository;

    public Form clone(Form origin, UUID userId, UUID previousVersionId, UUID rootFormId, int version) {
        Form newForm = Form.builder()
                .tenantId(origin.getTenantId())
                .name(origin.getName())
                .description(origin.getDescription())
                .type(origin.getType())
                .timeLimitSeconds(origin.getTimeLimitSeconds())
                .previousVersionId(previousVersionId)
                .rootFormId(rootFormId)
                .version(version)
                .createdBy(userId)
                .updatedBy(userId)
                .build();
        Form savedForm = formRepository.save(newForm);

        Map<UUID, UUID> sectionIdMap = cloneSections(origin, savedForm);
        cloneQuestions(origin, savedForm, sectionIdMap);

        return savedForm;
    }

    private Map<UUID, UUID> cloneSections(Form origin, Form newForm) {
        Map<UUID, UUID> sectionIdMap = new LinkedHashMap<>();
        for (FormSection section : origin.getSections()) {
            FormSection newSection = FormSection.builder()
                    .formId(newForm.getId())
                    .tenantId(origin.getTenantId())
                    .title(section.getTitle())
                    .description(section.getDescription())
                    .position(section.getPosition())
                    .timeLimitSeconds(section.getTimeLimitSeconds())
                    .build();
            FormSection saved = sectionRepository.save(newSection);
            sectionIdMap.put(section.getId(), saved.getId());
        }
        return sectionIdMap;
    }

    private void cloneQuestions(Form origin, Form newForm, Map<UUID, UUID> sectionIdMap) {
        List<FormQuestion> originalQuestions = origin.getSections().stream()
                .flatMap(s -> s.getQuestions().stream())
                .toList();

        Map<UUID, UUID> questionIdMap = new LinkedHashMap<>();
        Map<UUID, FormQuestion> savedByNewId = new LinkedHashMap<>();

        for (FormQuestion question : originalQuestions) {
            FormQuestion newQuestion = FormQuestion.builder()
                    .sectionId(sectionIdMap.get(question.getSectionId()))
                    .formId(newForm.getId())
                    .tenantId(origin.getTenantId())
                    .title(question.getTitle())
                    .description(question.getDescription())
                    .type(question.getType())
                    .position(question.getPosition())
                    .required(question.isRequired())
                    .categoryId(question.getCategoryId())
                    .timeLimitSeconds(question.getTimeLimitSeconds())
                    .config(question.getConfig())
                    .build();
            FormQuestion saved = questionRepository.save(newQuestion);
            questionIdMap.put(question.getId(), saved.getId());
            savedByNewId.put(saved.getId(), saved);
        }

        for (FormQuestion question : originalQuestions) {
            if (question.getConditionalLogic() == null) continue;
            FormQuestion saved = savedByNewId.get(questionIdMap.get(question.getId()));
            saved.setConditionalLogic(remapConditionalLogic(question.getConditionalLogic(), questionIdMap));
            questionRepository.save(saved);
        }
    }

    private ConditionalLogic remapConditionalLogic(ConditionalLogic original, Map<UUID, UUID> questionIdMap) {
        List<Condition> remapped = original.conditions().stream()
                .map(c -> new Condition(
                        questionIdMap.getOrDefault(c.sourceQuestionId(), c.sourceQuestionId()),
                        c.operator(), c.value()))
                .toList();
        return new ConditionalLogic(original.action(), original.logicOperator(), remapped);
    }
}
