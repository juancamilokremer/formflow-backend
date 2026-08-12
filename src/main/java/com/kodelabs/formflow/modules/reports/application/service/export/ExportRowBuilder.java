package com.kodelabs.formflow.modules.reports.application.service.export;

import com.kodelabs.formflow.modules.reports.domain.model.ExportableForm;
import com.kodelabs.formflow.modules.reports.domain.model.ExportableFormData;
import com.kodelabs.formflow.modules.reports.domain.model.ExportableQuestion;
import com.kodelabs.formflow.modules.reports.domain.model.ExportableResponse;
import com.kodelabs.formflow.shared.i18n.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Format-agnostic: turns the exportable data into a plain grid, row 0 = headers. */
@Component
@RequiredArgsConstructor
public class ExportRowBuilder {

    private static final DateTimeFormatter SUBMITTED_AT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneOffset.UTC);

    private final Messages messages;

    public List<List<String>> build(ExportableFormData data) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(header(data.form()));
        for (ExportableResponse response : data.responses()) {
            rows.add(row(data.form(), response));
        }
        return rows;
    }

    private List<String> header(ExportableForm form) {
        List<String> header = new ArrayList<>();
        header.add(messages.get("export.header.submitted_at"));
        form.questions().forEach(q -> header.add(q.title()));
        return header;
    }

    private List<String> row(ExportableForm form, ExportableResponse response) {
        List<String> row = new ArrayList<>();
        row.add(SUBMITTED_AT_FORMAT.format(response.submittedAt()));
        for (ExportableQuestion question : form.questions()) {
            row.add(response.displayValuesByQuestionId().getOrDefault(question.id(), ""));
        }
        return row;
    }
}
