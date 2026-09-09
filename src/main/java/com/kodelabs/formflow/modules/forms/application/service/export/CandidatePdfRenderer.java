package com.kodelabs.formflow.modules.forms.application.service.export;

import com.kodelabs.formflow.shared.export.HtmlToPdfRenderer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Typed entry point for rendering a candidate's convocatoria response as PDF — delegates the
 * actual Thymeleaf+Flying-Saucer mechanics to HtmlToPdfRenderer (shared, domain-agnostic) and
 * only owns the "pdf/candidate-response" template name and how CandidatePdfData maps to its
 * template variables.
 */
@Component
@RequiredArgsConstructor
public class CandidatePdfRenderer {

    private static final String TEMPLATE = "pdf/candidate-response";

    private final HtmlToPdfRenderer htmlToPdfRenderer;

    public byte[] render(CandidatePdfData data) {
        return htmlToPdfRenderer.render(TEMPLATE, toVariables(data));
    }

    private Map<String, Object> toVariables(CandidatePdfData data) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("candidateName", data.candidateName());
        variables.put("candidateEmail", data.candidateEmail());
        variables.put("convocatoriaName", data.convocatoriaName());
        variables.put("totalScore", data.totalScore());
        variables.put("classification", data.classification());
        variables.put("forms", data.forms());
        return variables;
    }
}
