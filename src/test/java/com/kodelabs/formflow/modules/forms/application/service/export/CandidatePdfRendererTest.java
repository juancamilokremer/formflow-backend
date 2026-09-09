package com.kodelabs.formflow.modules.forms.application.service.export;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.AnswerDetailResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidateFormExportResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ResponseCategoryScoreResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CandidatePdfRendererTest {

    private CandidatePdfRenderer renderer;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());

        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        renderer = new CandidatePdfRenderer(templateEngine);
    }

    @Test
    void rendersAValidPdfWithFormsAndAnswers() {
        CandidateFormExportResult form = new CandidateFormExportResult(
                "Evaluación técnica", 85.0,
                List.of(new ResponseCategoryScoreResult(UUID.randomUUID(), "Competencias Técnicas", 34.0)),
                List.of(new AnswerDetailResult(UUID.randomUUID(), "¿Años de experiencia?", "single", "opt2", "3-5 años")));

        byte[] pdf = renderer.render(new CandidatePdfData(
                "María Gómez", "maria@test.com", "Analista de RRHH", 85.0, "APTO", List.of(form)));

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }

    @Test
    void rendersWithoutFailingWhenCandidateHasNoResponses() {
        byte[] pdf = renderer.render(new CandidatePdfData(
                "Pedro Sin Responder", "pedro@test.com", "Analista de RRHH", null, null, List.of()));

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }
}
