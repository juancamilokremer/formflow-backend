package com.kodelabs.formflow.modules.reports.application.service.export;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/** Shared by every ResponseExporter so all formats name their files the same way. */
final class ExportFilenames {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);

    private ExportFilenames() {}

    static String build(String formName, String extension) {
        String slug = formName.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        return slug + "_" + DATE.format(Instant.now()) + "." + extension;
    }
}
