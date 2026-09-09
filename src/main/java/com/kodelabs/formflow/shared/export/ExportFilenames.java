package com.kodelabs.formflow.shared.export;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/** Shared by every export across the app so all downloaded files name themselves the same way. */
public final class ExportFilenames {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);

    private ExportFilenames() {}

    public static String build(String name, String extension) {
        String slug = name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        return slug + "_" + DATE.format(Instant.now()) + "." + extension;
    }
}
