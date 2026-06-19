package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class CsvParserService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

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
        String line;
        int lineNumber = 0;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (lineNumber == 1 && isHeader(line)) continue;
            if (!line.isBlank()) results.add(parseLine(line, lineNumber));
        }
        return results;
    }

    private boolean isHeader(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 2) return false;
        String col1 = parts[0].trim().toLowerCase();
        String col2 = parts[1].trim().toLowerCase();
        boolean col1IsLabel = col1.equals("nombre") || col1.equals("name") || col1.equals("candidato");
        boolean col2IsLabel = col2.equals("email") || col2.equals("correo") || col2.equals("e-mail");
        return col1IsLabel || col2IsLabel;
    }

    private CsvCandidate parseLine(String line, int lineNumber) {
        String[] parts = line.split(",", -1);
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
