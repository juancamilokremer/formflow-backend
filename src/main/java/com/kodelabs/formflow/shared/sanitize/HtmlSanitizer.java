package com.kodelabs.formflow.shared.sanitize;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;

/**
 * Strips HTML/script markup from free-text fields (form names, question text,
 * candidate names, tenant names) before they're persisted. These are plain-text
 * labels, never rich text, so no tags are allowed through — this guards against
 * stored XSS reaching a future unescaped consumer (export, integration, etc.),
 * on top of the output encoding Angular/Thymeleaf already apply.
 */
@Component
public class HtmlSanitizer {

    private final PolicyFactory policy = new HtmlPolicyBuilder().toFactory();

    public String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return policy.sanitize(input);
    }
}
