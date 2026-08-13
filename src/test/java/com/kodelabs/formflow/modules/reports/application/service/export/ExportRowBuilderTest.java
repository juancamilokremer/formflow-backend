package com.kodelabs.formflow.modules.reports.application.service.export;

import com.kodelabs.formflow.modules.reports.domain.model.ExportableForm;
import com.kodelabs.formflow.modules.reports.domain.model.ExportableFormData;
import com.kodelabs.formflow.modules.reports.domain.model.ExportableQuestion;
import com.kodelabs.formflow.modules.reports.domain.model.ExportableResponse;
import com.kodelabs.formflow.shared.i18n.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ExportRowBuilderTest {

    @Mock private Messages messages;
    @InjectMocks private ExportRowBuilder builder;

    @BeforeEach
    void setUp() {
        lenient().when(messages.get("export.header.submitted_at")).thenReturn("Fecha de envío");
    }

    @Test
    void firstRowIsHeaderWithSubmittedAtFollowedByQuestionTitles() {
        UUID q1 = UUID.randomUUID();
        UUID q2 = UUID.randomUUID();
        ExportableFormData data = new ExportableFormData(
                new ExportableForm("Encuesta", List.of(
                        new ExportableQuestion(q1, "Nombre"),
                        new ExportableQuestion(q2, "Edad"))),
                List.of());

        List<List<String>> rows = builder.build(data, ZoneOffset.UTC);

        assertThat(rows.get(0)).containsExactly("Fecha de envío", "Nombre", "Edad");
    }

    @Test
    void eachResponseBecomesARowAlignedToQuestionOrder() {
        UUID q1 = UUID.randomUUID();
        UUID q2 = UUID.randomUUID();
        ExportableForm form = new ExportableForm("Encuesta", List.of(
                new ExportableQuestion(q1, "Nombre"),
                new ExportableQuestion(q2, "Edad")));
        ExportableResponse response = new ExportableResponse(
                Instant.parse("2026-08-01T10:00:00Z"),
                Map.of(q1, "Ana", q2, "30"));

        List<List<String>> rows = builder.build(new ExportableFormData(form, List.of(response)), ZoneOffset.UTC);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(1)).containsExactly("2026-08-01 10:00", "Ana", "30");
    }

    @Test
    void formatsSubmittedAtInTheCallersTimezoneNotTheServersOwn() {
        ExportableForm form = new ExportableForm("Encuesta", List.of());
        ExportableResponse response = new ExportableResponse(Instant.parse("2026-08-01T10:00:00Z"), Map.of());
        ExportableFormData data = new ExportableFormData(form, List.of(response));

        List<List<String>> inBogota = builder.build(data, ZoneId.of("America/Bogota"));
        List<List<String>> inTokyo = builder.build(data, ZoneId.of("Asia/Tokyo"));

        // Same instant, different caller-supplied zone -> different rendered text.
        assertThat(inBogota.get(1).get(0)).isEqualTo("2026-08-01 05:00");
        assertThat(inTokyo.get(1).get(0)).isEqualTo("2026-08-01 19:00");
    }

    @Test
    void unansweredQuestionsProduceAnEmptyCellNotAMissingOne() {
        UUID q1 = UUID.randomUUID();
        UUID q2 = UUID.randomUUID();
        ExportableForm form = new ExportableForm("Encuesta", List.of(
                new ExportableQuestion(q1, "Nombre"),
                new ExportableQuestion(q2, "Edad")));
        ExportableResponse response = new ExportableResponse(
                Instant.parse("2026-08-01T10:00:00Z"),
                Map.of(q1, "Ana"));

        List<List<String>> rows = builder.build(new ExportableFormData(form, List.of(response)), ZoneOffset.UTC);

        assertThat(rows.get(1)).containsExactly("2026-08-01 10:00", "Ana", "");
    }

    @Test
    void formWithNoResponsesStillProducesTheHeaderRow() {
        ExportableForm form = new ExportableForm("Encuesta", List.of());

        List<List<String>> rows = builder.build(new ExportableFormData(form, List.of()), ZoneOffset.UTC);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0)).containsExactly("Fecha de envío");
    }
}
