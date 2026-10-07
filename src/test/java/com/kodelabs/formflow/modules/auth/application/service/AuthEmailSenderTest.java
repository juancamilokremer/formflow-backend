package com.kodelabs.formflow.modules.auth.application.service;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.notifications.domain.model.EmailType;
import com.kodelabs.formflow.modules.notifications.domain.port.in.SendEmailUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthEmailSenderTest {

    @Mock private EmailTokenIssuer emailTokenIssuer;
    @Mock private SendEmailUseCase sendEmail;

    private AuthEmailSender authEmailSender;

    @BeforeEach
    void setUp() {
        authEmailSender = new AuthEmailSender(emailTokenIssuer, sendEmail);
        ReflectionTestUtils.setField(authEmailSender, "frontendBaseUrl", "http://localhost:4200");
        ReflectionTestUtils.setField(authEmailSender, "docsUrl", "https://docs.formflow.app/placeholder");
    }

    @Test
    void sendWelcomeIncludesTheDocsUrlInTheModel() {
        User user = User.builder().id(UUID.randomUUID()).email("admin@abc.com").firstName("Juan").build();
        Tenant tenant = Tenant.builder().id(UUID.randomUUID()).name("Empresa ABC").build();

        authEmailSender.sendWelcome(user, tenant);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> modelCaptor = ArgumentCaptor.forClass(Map.class);
        verify(sendEmail).send(eq(EmailType.WELCOME), eq("admin@abc.com"), modelCaptor.capture());

        assertThat(modelCaptor.getValue())
                .containsEntry("userName", "Juan")
                .containsEntry("tenantName", "Empresa ABC")
                .containsEntry("appUrl", "http://localhost:4200")
                .containsEntry("docsUrl", "https://docs.formflow.app/placeholder");
    }
}
