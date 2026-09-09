package com.kodelabs.formflow.modules.forms.application.service.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;

/**
 * Renders a candidate's convocatoria response as PDF: a Thymeleaf template produces well-formed
 * XHTML, which Flying Saucer (backed by OpenPDF) lays out and rasterizes to PDF bytes. Kept
 * separate from EmailTemplateService/TemplateRendererPort — that port is scoped to modules/notifications
 * and hardcodes the "email/" template prefix; PDF templates live in their own "pdf/" resource folder.
 */
@Component
@RequiredArgsConstructor
public class CandidatePdfRenderer {

    private static final String TEMPLATE = "pdf/candidate-response";

    private final TemplateEngine templateEngine;

    public byte[] render(CandidatePdfData data) {
        String html = templateEngine.process(TEMPLATE, toContext(data));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(out);
        return out.toByteArray();
    }

    private Context toContext(CandidatePdfData data) {
        Context context = new Context();
        context.setVariable("candidateName", data.candidateName());
        context.setVariable("candidateEmail", data.candidateEmail());
        context.setVariable("convocatoriaName", data.convocatoriaName());
        context.setVariable("totalScore", data.totalScore());
        context.setVariable("classification", data.classification());
        context.setVariable("forms", data.forms());
        return context;
    }
}
