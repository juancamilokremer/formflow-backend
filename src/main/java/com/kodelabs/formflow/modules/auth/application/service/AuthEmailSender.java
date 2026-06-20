package com.kodelabs.formflow.modules.auth.application.service;

import com.kodelabs.formflow.modules.auth.domain.model.EmailTokenType;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.notifications.domain.model.EmailType;
import com.kodelabs.formflow.modules.notifications.domain.port.in.SendEmailUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Auth-module emails: issues the token, builds the frontend link and
 * delegates to the notifications module through its input port.
 */
@Component
@RequiredArgsConstructor
public class AuthEmailSender {

    public static final Duration RESET_TOKEN_VALIDITY = Duration.ofHours(1);
    public static final Duration VERIFICATION_TOKEN_VALIDITY = Duration.ofHours(24);

    private static final String MODEL_KEY_USERNAME = "userName";

    private final EmailTokenIssuer emailTokenIssuer;
    private final SendEmailUseCase sendEmail;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    public void sendWelcome(User user, Tenant tenant) {
        Map<String, Object> model = new HashMap<>();
        model.put(MODEL_KEY_USERNAME, user.getFirstName());
        model.put("tenantName", tenant.getName());
        model.put("appUrl", frontendBaseUrl);
        sendEmail.send(EmailType.WELCOME, user.getEmail(), model);
    }

    public void sendEmailVerification(User user) {
        String rawToken = emailTokenIssuer.issue(user, EmailTokenType.EMAIL_VERIFICATION,
                VERIFICATION_TOKEN_VALIDITY);
        Map<String, Object> model = new HashMap<>();
        model.put(MODEL_KEY_USERNAME, user.getFirstName());
        model.put("verificationUrl", frontendBaseUrl + "/verify-email?token=" + rawToken);
        model.put("expirationHours", VERIFICATION_TOKEN_VALIDITY.toHours());
        sendEmail.send(EmailType.EMAIL_VERIFICATION, user.getEmail(), model);
    }

    public void sendPasswordReset(User user) {
        String rawToken = emailTokenIssuer.issue(user, EmailTokenType.PASSWORD_RESET,
                RESET_TOKEN_VALIDITY);
        Map<String, Object> model = new HashMap<>();
        model.put(MODEL_KEY_USERNAME, user.getFirstName());
        model.put("resetUrl", frontendBaseUrl + "/reset-password?token=" + rawToken);
        model.put("expirationHours", RESET_TOKEN_VALIDITY.toHours());
        sendEmail.send(EmailType.PASSWORD_RESET, user.getEmail(), model);
    }
}
