package com.kodelabs.formflow.modules.notifications.infrastructure.email;

import com.kodelabs.formflow.modules.notifications.domain.port.out.TemplateRendererPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * Thymeleaf adapter of TemplateRendererPort (templates in resources/templates/email/).
 * Template texts come from MessageSource (#{key}), so adding a language
 * only requires a new messages_{lang}.properties file.
 */
@Service
@RequiredArgsConstructor
public class EmailTemplateService implements TemplateRendererPort {

    public static final String TEMPLATE_WELCOME = "welcome";
    public static final String TEMPLATE_RESET_PASSWORD = "reset-password";
    public static final String TEMPLATE_EMAIL_VERIFICATION = "email-verification";

    private final TemplateEngine templateEngine;

    @Override
    public String render(String templateName, Map<String, Object> variables) {
        Context context = new Context(LocaleContextHolder.getLocale());
        context.setVariables(variables);
        return templateEngine.process("email/" + templateName, context);
    }
}
