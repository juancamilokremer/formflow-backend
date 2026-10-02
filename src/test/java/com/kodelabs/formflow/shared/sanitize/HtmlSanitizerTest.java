package com.kodelabs.formflow.shared.sanitize;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlSanitizerTest {

    private final HtmlSanitizer sanitizer = new HtmlSanitizer();

    @Test
    void stripsScriptTagsAndTheirContent() {
        assertThat(sanitizer.sanitize("<script>alert(1)</script>Hola"))
                .isEqualTo("Hola");
    }

    @Test
    void stripsFormattingTagsKeepingPlainText() {
        assertThat(sanitizer.sanitize("<b>Negrita</b> y <i>cursiva</i>"))
                .isEqualTo("Negrita y cursiva");
    }

    @Test
    void stripsAttributesLikeOnError() {
        assertThat(sanitizer.sanitize("<img src=x onerror=alert(1)>"))
                .doesNotContain("onerror")
                .doesNotContain("<img");
    }

    @Test
    void leavesPlainTextUnchanged() {
        assertThat(sanitizer.sanitize("Evaluación de Candidatos 2026")).isEqualTo("Evaluación de Candidatos 2026");
    }

    @Test
    void returnsNullForNullInput() {
        assertThat(sanitizer.sanitize(null)).isNull();
    }
}
