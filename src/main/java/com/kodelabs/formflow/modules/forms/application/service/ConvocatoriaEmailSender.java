package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.TenantInfo;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.port.out.TenantInfoPort;
import com.kodelabs.formflow.modules.notifications.domain.model.EmailType;
import com.kodelabs.formflow.modules.notifications.domain.port.in.SendEmailUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConvocatoriaEmailSender {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.of("America/Bogota"));

    private final SendEmailUseCase sendEmail;
    private final TenantInfoPort tenantInfoPort;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    public void sendInvitation(Candidate candidate, Convocatoria convocatoria) {
        TenantInfo tenant = resolveTenantInfo(convocatoria.getTenantId());
        Map<String, Object> model = new HashMap<>();
        model.put("candidateName", candidate.getName());
        model.put("convocatoriaName", convocatoria.getName());
        model.put("tenantName", tenant.name());
        model.put("surveyUrl", buildSurveyUrl(convocatoria.getId(), candidate.getToken()));
        sendEmail.send(EmailType.CANDIDATE_INVITATION, candidate.getEmail(), model);
    }

    public void sendReminder(Candidate candidate, Convocatoria convocatoria) {
        TenantInfo tenant = resolveTenantInfo(convocatoria.getTenantId());
        Map<String, Object> model = new HashMap<>();
        model.put("candidateName", candidate.getName());
        model.put("convocatoriaName", convocatoria.getName());
        model.put("tenantName", tenant.name());
        model.put("surveyUrl", buildSurveyUrl(convocatoria.getId(), candidate.getToken()));
        model.put("endDate", convocatoria.getEndDate() != null
                ? DATE_FORMAT.format(convocatoria.getEndDate()) : null);
        sendEmail.send(EmailType.CANDIDATE_REMINDER, candidate.getEmail(), model);
    }

    public void sendResponseConfirmation(Candidate candidate, Convocatoria convocatoria) {
        TenantInfo tenant = resolveTenantInfo(convocatoria.getTenantId());
        Map<String, Object> model = new HashMap<>();
        model.put("candidateName", candidate.getName());
        model.put("convocatoriaName", convocatoria.getName());
        model.put("tenantName", tenant.name());
        sendEmail.send(EmailType.CANDIDATE_RESPONSE_CONFIRMATION, candidate.getEmail(), model);
    }

    public void sendAdminNotification(Candidate candidate, Convocatoria convocatoria) {
        TenantInfo tenant = resolveTenantInfo(convocatoria.getTenantId());
        if (tenant.adminEmail() == null) {
            log.warn("No admin email found for tenant {}, skipping admin notification", convocatoria.getTenantId());
            return;
        }
        Map<String, Object> model = new HashMap<>();
        model.put("candidateName", candidate.getName());
        model.put("candidateEmail", candidate.getEmail());
        model.put("convocatoriaName", convocatoria.getName());
        model.put("rankingUrl", frontendBaseUrl + "/convocatorias/" + convocatoria.getId() + "/ranking");
        sendEmail.send(EmailType.ADMIN_CANDIDATE_RESPONDED, tenant.adminEmail(), model);
    }

    private TenantInfo resolveTenantInfo(UUID tenantId) {
        return tenantInfoPort.findByTenantId(tenantId)
                .orElse(new TenantInfo("FormFlow", null));
    }

    private String buildSurveyUrl(UUID convocatoriaId, UUID candidateToken) {
        return frontendBaseUrl + "/r/" + convocatoriaId + "/" + candidateToken;
    }
}
