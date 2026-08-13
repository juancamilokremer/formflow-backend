package com.kodelabs.formflow.modules.reports.application.service.export;

import com.kodelabs.formflow.modules.reports.domain.model.ExportableForm;
import com.kodelabs.formflow.modules.reports.domain.model.ExportableFormData;
import com.kodelabs.formflow.modules.reports.domain.model.ExportableQuestion;
import com.kodelabs.formflow.modules.reports.domain.model.ExportableResponse;
import com.kodelabs.formflow.shared.i18n.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Format-agnostic: turns the exportable data into a plain grid, row 0 = headers. */
@Component
@RequiredArgsConstructor
public class ExportRowBuilder {

    // Responses are stored as UTC instants. The zone comes from the caller (the
    // browser knows its own IANA zone) so the export matches whatever the user
    // is already seeing on screen, regardless of where the server runs.
    private static final DateTimeFormatter SUBMITTED_AT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Messages messages;

    public List<List<String>> build(ExportableFormData data, ZoneId timezone) {
        DateTimeFormatter formatter = SUBMITTED_AT_FORMAT.withZone(timezone);
        List<List<String>> rows = new ArrayList<>();
        rows.add(header(data.form()));
        for (ExportableResponse response : data.responses()) {
            rows.add(row(data.form(), response, formatter));
        }
        return rows;
    }

    private List<String> header(ExportableForm form) {
        List<String> header = new ArrayList<>();
        header.add(messages.get("export.header.submitted_at"));
        form.questions().forEach(q -> header.add(q.title()));
        return header;
    }

    private List<String> row(ExportableForm form, ExportableResponse response, DateTimeFormatter formatter) {
        List<String> row = new ArrayList<>();
        row.add(formatter.format(response.submittedAt()));
        for (ExportableQuestion question : form.questions()) {
            row.add(response.displayValuesByQuestionId().getOrDefault(question.id(), ""));
        }
        return row;
    }
}
