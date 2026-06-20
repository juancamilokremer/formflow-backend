package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvParserServiceTest {

    private final CsvParserService parser = new CsvParserService();

    @Test
    void parsesValidCsvWithoutHeader() {
        byte[] csv = "María García,maria@test.com\nCarlos Ruiz,carlos@test.com".getBytes(StandardCharsets.UTF_8);
        List<CsvParserService.CsvCandidate> result = parser.parse(csv);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("María García");
        assertThat(result.get(0).email()).isEqualTo("maria@test.com");
    }

    @Test
    void skipsHeaderRowWhenPresent() {
        byte[] csv = "nombre,email\nMaría García,maria@test.com".getBytes(StandardCharsets.UTF_8);
        List<CsvParserService.CsvCandidate> result = parser.parse(csv);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("María García");
    }

    @Test
    void throwsBadRequestOnInvalidEmailFormat() {
        byte[] csv = "Pedro,not-an-email".getBytes(StandardCharsets.UTF_8);
        assertThatThrownBy(() -> parser.parse(csv))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void throwsBadRequestWhenRowHasOnlyOneColumn() {
        byte[] csv = "SoloNombre".getBytes(StandardCharsets.UTF_8);
        assertThatThrownBy(() -> parser.parse(csv))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void ignoresBlankLines() {
        byte[] csv = "Ana,ana@test.com\n\nLuis,luis@test.com\n".getBytes(StandardCharsets.UTF_8);
        List<CsvParserService.CsvCandidate> result = parser.parse(csv);
        assertThat(result).hasSize(2);
    }

    @Test
    void parsesEmptyCsvToEmptyList() {
        byte[] csv = "".getBytes(StandardCharsets.UTF_8);
        List<CsvParserService.CsvCandidate> result = parser.parse(csv);
        assertThat(result).isEmpty();
    }
}
