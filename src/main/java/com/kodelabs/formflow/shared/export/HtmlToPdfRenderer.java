package com.kodelabs.formflow.shared.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * Generic HTML-to-PDF engine: processes a Thymeleaf template (resources/templates/{name}.html,
 * must render well-formed XHTML) into PDF bytes via Flying Saucer (backed by OpenPDF). Has no
 * knowledge of any specific document's content — every PDF export in the app should go through
 * this instead of embedding the Thymeleaf+Flying-Saucer mechanics again.
 */
@Component
@RequiredArgsConstructor
public class HtmlToPdfRenderer {

    private final TemplateEngine templateEngine;

    public byte[] render(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        String html = templateEngine.process(templateName, context);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(out);
        return out.toByteArray();
    }
}
