package com.kodelabs.formflow.modules.forms.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.forms.application.service.AnswerDisplayFormatter;
import com.kodelabs.formflow.modules.forms.application.service.FormSnapshotBuilder;
import com.kodelabs.formflow.modules.forms.domain.model.AnswerValue;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.FormSnapshot;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.QuestionSnapshot;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.SectionSnapshot;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.modules.reports.domain.model.ExportableForm;
import com.kodelabs.formflow.modules.reports.domain.model.ExportableFormData;
import com.kodelabs.formflow.modules.reports.domain.model.ExportableQuestion;
import com.kodelabs.formflow.modules.reports.domain.model.ExportableResponse;
import com.kodelabs.formflow.modules.reports.domain.port.out.FormResponseDataPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Anti-corruption layer: implements reports' FormResponseDataPort from forms'
 * own building blocks (snapshot + answer formatting), same pattern as
 * TenantInfoAdapter for auth -> forms. This is the only class in the codebase
 * that knows about both modules.
 */
@Component
@RequiredArgsConstructor
public class FormResponseDataAdapter implements FormResponseDataPort {

    private static final String INFO_QUESTION_TYPE = "info";

    private final FormSnapshotBuilder snapshotBuilder;
    private final FormResponseRepositoryPort responseRepository;
    private final AnswerDisplayFormatter answerDisplayFormatter;

    @Override
    public ExportableFormData load(UUID formId, UUID tenantId) {
        FormSnapshot snapshot = snapshotBuilder.build(formId, tenantId);
        List<QuestionSnapshot> questions = orderedExportableQuestions(snapshot);
        List<FormResponse> responses = responseRepository.findAllByFormIdAndTenantId(formId, tenantId);

        ExportableForm form = new ExportableForm(
                snapshot.formName(),
                questions.stream().map(q -> new ExportableQuestion(q.id(), q.title())).toList());
        List<ExportableResponse> exportableResponses = responses.stream()
                .map(response -> toExportableResponse(response, questions))
                .toList();

        return new ExportableFormData(form, exportableResponses);
    }

    private List<QuestionSnapshot> orderedExportableQuestions(FormSnapshot snapshot) {
        return snapshot.sections().stream()
                .sorted(Comparator.comparingInt(SectionSnapshot::position))
                .flatMap(section -> section.questions().stream()
                        .sorted(Comparator.comparingInt(QuestionSnapshot::position)))
                .filter(question -> !INFO_QUESTION_TYPE.equals(question.type()))
                .toList();
    }

    private ExportableResponse toExportableResponse(FormResponse response, List<QuestionSnapshot> questions) {
        Map<UUID, Object> valuesByQuestion = response.getAnswers().stream()
                .collect(Collectors.toMap(AnswerValue::getQuestionId, AnswerValue::getValue));

        Map<UUID, String> displayValues = new LinkedHashMap<>();
        for (QuestionSnapshot question : questions) {
            String displayValue = answerDisplayFormatter.format(question, valuesByQuestion.get(question.id()));
            if (displayValue != null) displayValues.put(question.id(), displayValue);
        }
        return new ExportableResponse(response.getSubmittedAt(), displayValues);
    }
}
