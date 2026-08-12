package com.kodelabs.formflow.modules.forms.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.forms.application.service.AnswerDisplayFormatter;
import com.kodelabs.formflow.modules.forms.application.service.FormSnapshotBuilder;
import com.kodelabs.formflow.modules.forms.domain.model.AnswerValue;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.FormSnapshot;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.QuestionSnapshot;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.SectionSnapshot;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.modules.reports.domain.model.ExportableFormData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormResponseDataAdapterTest {

    @Mock private FormSnapshotBuilder snapshotBuilder;
    @Mock private FormResponseRepositoryPort responseRepository;
    @Mock private AnswerDisplayFormatter answerDisplayFormatter;
    @InjectMocks private FormResponseDataAdapter adapter;

    private UUID formId;
    private UUID tenantId;
    private UUID infoId;
    private UUID q1;
    private UUID q2;

    @BeforeEach
    void setUp() {
        formId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        infoId = UUID.randomUUID();
        q1 = UUID.randomUUID();
        q2 = UUID.randomUUID();
    }

    private QuestionSnapshot question(UUID id, String type, int position, String title) {
        return new QuestionSnapshot(id, title, null, type, position, false, null, null, Map.of());
    }

    @Test
    void ordersQuestionsBySectionAndPositionAndSkipsInfo() {
        SectionSnapshot sectionB = new SectionSnapshot(UUID.randomUUID(), "B", null, 1, null,
                List.of(question(q2, "text", 0, "Segunda")));
        SectionSnapshot sectionA = new SectionSnapshot(UUID.randomUUID(), "A", null, 0, null,
                List.of(question(infoId, "info", 1, "Info"), question(q1, "text", 0, "Primera")));
        FormSnapshot snapshot = new FormSnapshot(formId, "Encuesta", "REGISTRATION", 1, Instant.now(),
                List.of(sectionB, sectionA));

        when(snapshotBuilder.build(formId, tenantId)).thenReturn(snapshot);
        when(responseRepository.findAllByFormIdAndTenantId(formId, tenantId)).thenReturn(List.of());

        ExportableFormData data = adapter.load(formId, tenantId);

        assertThat(data.form().questions()).extracting("id").containsExactly(q1, q2);
        assertThat(data.form().formName()).isEqualTo("Encuesta");
    }

    @Test
    void resolvesDisplayValuesPerAnswerUsingAnswerDisplayFormatter() {
        QuestionSnapshot question1 = question(q1, "single", 0, "Nivel");
        SectionSnapshot section = new SectionSnapshot(UUID.randomUUID(), "A", null, 0, null, List.of(question1));
        FormSnapshot snapshot = new FormSnapshot(formId, "Encuesta", "REGISTRATION", 1, Instant.now(), List.of(section));

        FormResponse response = FormResponse.builder()
                .id(UUID.randomUUID()).formId(formId).tenantId(tenantId)
                .respondentToken(UUID.randomUUID())
                .submittedAt(Instant.parse("2026-08-01T10:00:00Z"))
                .answers(List.of(AnswerValue.builder().questionId(q1).value("opt-senior").build()))
                .build();

        when(snapshotBuilder.build(formId, tenantId)).thenReturn(snapshot);
        when(responseRepository.findAllByFormIdAndTenantId(formId, tenantId)).thenReturn(List.of(response));
        when(answerDisplayFormatter.format(question1, "opt-senior")).thenReturn("Senior");

        ExportableFormData data = adapter.load(formId, tenantId);

        assertThat(data.responses()).hasSize(1);
        assertThat(data.responses().get(0).submittedAt()).isEqualTo(Instant.parse("2026-08-01T10:00:00Z"));
        assertThat(data.responses().get(0).displayValuesByQuestionId()).containsEntry(q1, "Senior");
    }

    @Test
    void unansweredQuestionsAreOmittedFromDisplayValues() {
        QuestionSnapshot question1 = question(q1, "text", 0, "Comentarios");
        SectionSnapshot section = new SectionSnapshot(UUID.randomUUID(), "A", null, 0, null, List.of(question1));
        FormSnapshot snapshot = new FormSnapshot(formId, "Encuesta", "REGISTRATION", 1, Instant.now(), List.of(section));

        FormResponse response = FormResponse.builder()
                .id(UUID.randomUUID()).formId(formId).tenantId(tenantId)
                .respondentToken(UUID.randomUUID())
                .submittedAt(Instant.now())
                .answers(List.of())
                .build();

        when(snapshotBuilder.build(formId, tenantId)).thenReturn(snapshot);
        when(responseRepository.findAllByFormIdAndTenantId(formId, tenantId)).thenReturn(List.of(response));
        when(answerDisplayFormatter.format(any(), any())).thenReturn(null);

        ExportableFormData data = adapter.load(formId, tenantId);

        assertThat(data.responses().get(0).displayValuesByQuestionId()).isEmpty();
    }
}
