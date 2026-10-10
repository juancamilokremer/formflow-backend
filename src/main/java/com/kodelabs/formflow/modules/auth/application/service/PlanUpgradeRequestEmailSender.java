package com.kodelabs.formflow.modules.auth.application.service;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.notifications.domain.model.EmailType;
import com.kodelabs.formflow.modules.notifications.domain.port.in.SendEmailUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/** Notifies Kode Labs' sales address when a tenant requests a plan upgrade (backend#191). */
@Component
@RequiredArgsConstructor
public class PlanUpgradeRequestEmailSender {

    private final SendEmailUseCase sendEmail;

    @Value("${app.sales-email}")
    private String salesEmail;

    public void send(User requester, Tenant tenant, TenantPlan requestedPlan, String message) {
        Map<String, Object> model = new HashMap<>();
        model.put("tenantName", tenant.getName());
        model.put("requesterName", requester.getFullName());
        model.put("requesterEmail", requester.getEmail());
        model.put("currentPlan", tenant.getPlan());
        model.put("requestedPlan", requestedPlan);
        model.put("message", message);
        sendEmail.send(EmailType.PLAN_UPGRADE_REQUEST, salesEmail, model);
    }
}
