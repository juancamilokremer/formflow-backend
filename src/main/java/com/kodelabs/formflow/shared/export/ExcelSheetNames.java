package com.kodelabs.formflow.shared.export;

import java.util.Set;

/** Sanitizes and deduplicates Excel sheet names — POI throws if two sheets in the same workbook share a name. */
public final class ExcelSheetNames {

    private static final int MAX_LENGTH = 31;
    private static final String FORBIDDEN_CHARS = "[:\\\\/?*\\[\\]]";

    private ExcelSheetNames() {}

    public static String uniqueName(String rawName, Set<String> alreadyUsed) {
        String base = sanitize(rawName);

        String candidate = base;
        int suffix = 2;
        while (alreadyUsed.contains(candidate)) {
            String suffixText = " (" + suffix + ")";
            String truncatedBase = base.length() + suffixText.length() > MAX_LENGTH
                    ? base.substring(0, MAX_LENGTH - suffixText.length())
                    : base;
            candidate = truncatedBase + suffixText;
            suffix++;
        }
        return candidate;
    }

    private static String sanitize(String rawName) {
        String cleaned = rawName.replaceAll(FORBIDDEN_CHARS, " ").trim();
        if (cleaned.isEmpty()) cleaned = "Hoja";
        return cleaned.length() > MAX_LENGTH ? cleaned.substring(0, MAX_LENGTH) : cleaned;
    }
}
