package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.shared.exception.BusinessException;
import com.kodelabs.formflow.shared.i18n.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsvParserServiceTest {

    @Mock private Messages messages;
    @InjectMocks private CsvParserService parser;

    @BeforeEach
    void setUp() {
        lenient().when(messages.get("csv.header.name_labels")).thenReturn("nombre,name,candidato");
        lenient().when(messages.get("csv.header.email_labels")).thenReturn("email,correo,e-mail");
    }

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

    @Test
    void parsesSemicolonDelimitedCsvWithSpanishHeader() {
        byte[] csv = "candidato;correo\nJuan Camilo Kremer;juancamilokremer@gmail.com\nTatiana Morales;tatiana.m.calderon@gmail.com"
                .getBytes(StandardCharsets.UTF_8);
        List<CsvParserService.CsvCandidate> result = parser.parse(csv);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Juan Camilo Kremer");
        assertThat(result.get(0).email()).isEqualTo("juancamilokremer@gmail.com");
        assertThat(result.get(1).name()).isEqualTo("Tatiana Morales");
    }

    @Test
    void stripsUtf8BomBeforeDetectingHeader() {
        byte[] csv = ("﻿candidato;correo\nAna Torres;ana@test.com").getBytes(StandardCharsets.UTF_8);
        List<CsvParserService.CsvCandidate> result = parser.parse(csv);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Ana Torres");
    }

    @Test
    void detectsCommaDelimiterWhenNoSemicolonsArePresent() {
        byte[] csv = "nombre,email\nMaría García,maria@test.com".getBytes(StandardCharsets.UTF_8);
        List<CsvParserService.CsvCandidate> result = parser.parse(csv);
        assertThat(result).hasSize(1);
    }
}
