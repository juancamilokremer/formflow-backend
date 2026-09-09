package com.kodelabs.formflow.shared.export;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Generic engine test — uses a StringTemplateResolver so it has no dependency on any concrete
 * template file on disk, proving this component knows nothing about any specific PDF's content.
 */
class HtmlToPdfRendererTest {

    private HtmlToPdfRenderer renderer;

    @BeforeEach
    void setUp() {
        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode(TemplateMode.HTML);

        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        renderer = new HtmlToPdfRenderer(templateEngine);
    }

    @Test
    void rendersATemplateWithVariablesIntoAValidPdf() {
        String template = """
                <!DOCTYPE html>
                <html xmlns:th="http://www.thymeleaf.org">
                <body><h1 th:text="${title}">placeholder</h1></body>
                </html>
                """;

        byte[] pdf = renderer.render(template, Map.of("title", "Hola PDF"));

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }

    @Test
    void rendersWithoutVariables() {
        String template = "<!DOCTYPE html><html><body><p>Sin variables</p></body></html>";

        byte[] pdf = renderer.render(template, Map.of());

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }
}
