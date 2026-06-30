package com.kodelabs.formflow.modules.forms.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.config.QuestionConfig;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.FormSnapshot;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.QuestionSnapshot;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.SectionSnapshot;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FormSnapshotBuilder {

    private final FormLoader formLoader;
    private final FormSectionRepositoryPort sectionRepository;
    private final FormQuestionRepositoryPort questionRepository;
    private final ObjectMapper objectMapper;

    public FormSnapshot build(UUID formId, UUID tenantId) {
        Form form = loadForm(formId, tenantId);
        List<FormSection> sections = loadSections(formId, tenantId);
        Map<UUID, List<FormQuestion>> questionsBySection = loadQuestions(sections);
        return assemble(form, sections, questionsBySection);
    }

    /** Builds a snapshot from an already-loaded form with sections and questions populated. */
    public FormSnapshot buildFromForm(Form form) {
        List<FormSection> sections = form.getSections();
        Map<UUID, List<FormQuestion>> questionsBySection = sections.stream()
                .collect(Collectors.toMap(
                        FormSection::getId,
                        s -> s.getQuestions() != null ? s.getQuestions() : List.of()));
        return assemble(form, sections, questionsBySection);
    }

    private Form loadForm(UUID formId, UUID tenantId) {
        return formLoader.loadOrThrow(formId, tenantId);
    }

    private List<FormSection> loadSections(UUID formId, UUID tenantId) {
        return sectionRepository.findActiveByFormIdAndTenantId(formId, tenantId);
    }

    private Map<UUID, List<FormQuestion>> loadQuestions(List<FormSection> sections) {
        List<UUID> sectionIds = sections.stream().map(FormSection::getId).toList();
        if (sectionIds.isEmpty()) return Map.of();
        return questionRepository.findAllActiveBySectionIds(sectionIds);
    }

    private FormSnapshot assemble(Form form, List<FormSection> sections,
                                  Map<UUID, List<FormQuestion>> questionsBySection) {
        List<SectionSnapshot> sectionSnapshots = sections.stream()
                .map(s -> toSectionSnapshot(s, questionsBySection.getOrDefault(s.getId(), List.of())))
                .toList();
        return new FormSnapshot(
                form.getId(),
                form.getName(),
                form.getType() != null ? form.getType().name() : null,
                form.getVersion(),
                Instant.now(),
                sectionSnapshots);
    }

    private SectionSnapshot toSectionSnapshot(FormSection section, List<FormQuestion> questions) {
        List<QuestionSnapshot> questionSnapshots = questions.stream()
                .map(this::toQuestionSnapshot)
                .toList();
        return new SectionSnapshot(section.getId(), section.getTitle(), section.getDescription(),
                section.getPosition(), section.getTimeLimitSeconds(), questionSnapshots);
    }

    private QuestionSnapshot toQuestionSnapshot(FormQuestion question) {
        return new QuestionSnapshot(
                question.getId(),
                question.getTitle(),
                question.getDescription(),
                question.getType() != null ? question.getType().code() : null,
                question.getPosition(),
                question.isRequired(),
                question.getCategoryId(),
                question.getTimeLimitSeconds(),
                serializeConfig(question.getConfig()));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> serializeConfig(QuestionConfig config) {
        if (config == null) return Map.of();
        return objectMapper.convertValue(config, Map.class);
    }
}
