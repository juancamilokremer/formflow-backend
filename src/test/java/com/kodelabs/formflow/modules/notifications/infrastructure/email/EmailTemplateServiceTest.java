package com.kodelabs.formflow.modules.notifications.infrastructure.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EmailTemplateServiceTest {

    private EmailTemplateService service;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        engine.setTemplateEngineMessageSource(messageSource);

        service = new EmailTemplateService(engine);
    }

    @Test
    void rendersWelcomeTemplateWithVariablesAndMessages() {
        String html = service.render(EmailTemplateService.TEMPLATE_WELCOME, Map.of(
                "userName", "Juan",
                "tenantName", "Empresa Demo",
                "appUrl", "https://app.formflow.app"));

        assertThat(html)
                .contains("Juan")
                .contains("Empresa Demo")
                .contains("https://app.formflow.app")
                .contains("FormFlow"); // resolved from #{email.footer} / header
    }

    @Test
    void rendersResetPasswordTemplateWithExpiration() {
        String html = service.render(EmailTemplateService.TEMPLATE_RESET_PASSWORD, Map.of(
                "userName", "Juan",
                "resetUrl", "https://app.formflow.app/reset?token=abc",
                "expirationHours", 1));

        assertThat(html)
                .contains("https://app.formflow.app/reset?token=abc")
                .contains("1");
    }

    @Test
    void rendersEmailVerificationTemplate() {
        String html = service.render(EmailTemplateService.TEMPLATE_EMAIL_VERIFICATION, Map.of(
                "userName", "Juan",
                "verificationUrl", "https://app.formflow.app/verify?token=xyz",
                "expirationHours", 24));

        assertThat(html).contains("https://app.formflow.app/verify?token=xyz");
    }
}
