package com.kodelabs.formflow.modules.auth.application.service;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PlanUpgradeRequestEmailSenderTest {

    @Mock private SendEmailUseCase sendEmail;

    private PlanUpgradeRequestEmailSender emailSender;

    @BeforeEach
    void setUp() {
        emailSender = new PlanUpgradeRequestEmailSender(sendEmail);
        ReflectionTestUtils.setField(emailSender, "salesEmail", "ventas@formflow.app");
    }

    @Test
    void sendBuildsTheModelFromTheRequesterTenantAndRequestedPlan() {
        User requester = User.builder()
                .id(UUID.randomUUID()).email("admin@abc.com").firstName("Juan").lastName("Perez").build();
        Tenant tenant = Tenant.builder()
                .id(UUID.randomUUID()).name("Empresa ABC").plan(TenantPlan.STARTER).build();

        emailSender.send(requester, tenant, TenantPlan.PRO, "Necesitamos más convocatorias");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> modelCaptor = ArgumentCaptor.forClass(Map.class);
        verify(sendEmail).send(eq(EmailType.PLAN_UPGRADE_REQUEST), eq("ventas@formflow.app"), modelCaptor.capture());

        assertThat(modelCaptor.getValue())
                .containsEntry("tenantName", "Empresa ABC")
                .containsEntry("requesterName", "Juan Perez")
                .containsEntry("requesterEmail", "admin@abc.com")
                .containsEntry("currentPlan", TenantPlan.STARTER)
                .containsEntry("requestedPlan", TenantPlan.PRO)
                .containsEntry("message", "Necesitamos más convocatorias");
    }

    @Test
    void sendAllowsANullMessage() {
        User requester = User.builder().id(UUID.randomUUID()).email("admin@abc.com").firstName("Juan").lastName("Perez").build();
        Tenant tenant = Tenant.builder().id(UUID.randomUUID()).name("Empresa ABC").plan(TenantPlan.FREE).build();

        emailSender.send(requester, tenant, TenantPlan.STARTER, null);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> modelCaptor = ArgumentCaptor.forClass(Map.class);
        verify(sendEmail).send(eq(EmailType.PLAN_UPGRADE_REQUEST), eq("ventas@formflow.app"), modelCaptor.capture());
        assertThat(modelCaptor.getValue().get("message")).isNull();
    }
}
