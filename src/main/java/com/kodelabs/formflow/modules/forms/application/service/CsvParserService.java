package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.shared.exception.BusinessException;
import com.kodelabs.formflow.shared.i18n.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CsvParserService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final char UTF8_BOM = '﻿';

    private final Messages messages;

    public record CsvCandidate(String name, String email) {}

    public List<CsvCandidate> parse(byte[] csvContent) {
        try (BufferedReader reader = openReader(csvContent)) {
            return readRows(reader);
        } catch (IOException e) {
            throw new BusinessException("error.convocatoria.csv_parse_error", HttpStatus.BAD_REQUEST);
        }
    }

    private BufferedReader openReader(byte[] csvContent) {
        return new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(csvContent), StandardCharsets.UTF_8));
    }

    private List<CsvCandidate> readRows(BufferedReader reader) throws IOException {
        List<CsvCandidate> results = new ArrayList<>();
        String firstLine = stripBom(reader.readLine());
        if (firstLine == null) return results;

        String delimiter = detectDelimiter(firstLine);
        int lineNumber = 1;
        if (!isHeader(firstLine, delimiter) && !firstLine.isBlank()) {
            results.add(parseLine(firstLine, delimiter, lineNumber));
        }

        String line;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (!line.isBlank()) results.add(parseLine(line, delimiter, lineNumber));
        }
        return results;
    }

    private String stripBom(String line) {
        if (line != null && !line.isEmpty() && line.charAt(0) == UTF8_BOM) {
            return line.substring(1);
        }
        return line;
    }

    /** A file uses one delimiter throughout — detected once from the header, from whichever appears more often. */
    private String detectDelimiter(String headerLine) {
        long commaCount = headerLine.chars().filter(c -> c == ',').count();
        long semicolonCount = headerLine.chars().filter(c -> c == ';').count();
        return semicolonCount > commaCount ? ";" : ",";
    }

    private boolean isHeader(String line, String delimiter) {
        String[] parts = line.split(delimiter, -1);
        if (parts.length < 2) return false;
        String col1 = parts[0].trim().toLowerCase();
        String col2 = parts[1].trim().toLowerCase();
        return headerLabels("csv.header.name_labels").contains(col1)
                || headerLabels("csv.header.email_labels").contains(col2);
    }

    private Set<String> headerLabels(String key) {
        return Arrays.stream(messages.get(key).split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    private CsvCandidate parseLine(String line, String delimiter, int lineNumber) {
        String[] parts = line.split(delimiter, -1);
        if (parts.length < 2) {
            throw new BusinessException("error.convocatoria.csv_invalid_row",
                    HttpStatus.BAD_REQUEST, lineNumber);
        }
        String name  = parts[0].trim();
        String email = parts[1].trim();
        if (name.isBlank()) {
            throw new BusinessException("error.convocatoria.csv_missing_name",
                    HttpStatus.BAD_REQUEST, lineNumber);
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException("error.convocatoria.csv_invalid_email",
                    HttpStatus.BAD_REQUEST, email, lineNumber);
        }
        return new CsvCandidate(name, email);
    }
}
