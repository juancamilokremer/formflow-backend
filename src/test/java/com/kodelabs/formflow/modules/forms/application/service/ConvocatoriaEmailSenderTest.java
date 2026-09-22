package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.TenantInfo;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.port.out.TenantInfoPort;
import com.kodelabs.formflow.modules.notifications.domain.model.EmailType;
import com.kodelabs.formflow.modules.notifications.domain.port.in.SendEmailUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConvocatoriaEmailSenderTest {

    @Mock private SendEmailUseCase sendEmail;
    @Mock private TenantInfoPort tenantInfoPort;

    @InjectMocks private ConvocatoriaEmailSender emailSender;

    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(tenantInfoPort.findByTenantId(tenantId))
                .thenReturn(Optional.of(new TenantInfo("FormFlow", "admin@test.com", null, null)));
    }

    @Test
    void marksModelAsSurveyForRegistrationType() {
        Convocatoria convocatoria = convocatoria(FormType.REGISTRATION);
        Candidate candidate = candidate(convocatoria.getId());

        emailSender.sendInvitation(candidate, convocatoria);
        emailSender.sendReminder(candidate, convocatoria);
        emailSender.sendResponseConfirmation(candidate, convocatoria);
        emailSender.sendAdminNotification(candidate, convocatoria);

        assertIsSurveyFlag(EmailType.CANDIDATE_INVITATION, true);
        assertIsSurveyFlag(EmailType.CANDIDATE_REMINDER, true);
        assertIsSurveyFlag(EmailType.CANDIDATE_RESPONSE_CONFIRMATION, true);
        assertIsSurveyFlag(EmailType.ADMIN_CANDIDATE_RESPONDED, true);
    }

    @Test
    void doesNotMarkModelAsSurveyForCandidatesType() {
        Convocatoria convocatoria = convocatoria(FormType.CANDIDATES);
        Candidate candidate = candidate(convocatoria.getId());

        emailSender.sendInvitation(candidate, convocatoria);
        emailSender.sendReminder(candidate, convocatoria);
        emailSender.sendResponseConfirmation(candidate, convocatoria);
        emailSender.sendAdminNotification(candidate, convocatoria);

        assertIsSurveyFlag(EmailType.CANDIDATE_INVITATION, false);
        assertIsSurveyFlag(EmailType.CANDIDATE_REMINDER, false);
        assertIsSurveyFlag(EmailType.CANDIDATE_RESPONSE_CONFIRMATION, false);
        assertIsSurveyFlag(EmailType.ADMIN_CANDIDATE_RESPONDED, false);
    }

    @Test
    void adminNotificationLinksToTheEncuestaDetailPageForRegistrationType() {
        Convocatoria convocatoria = convocatoria(FormType.REGISTRATION);
        Candidate candidate = candidate(convocatoria.getId());

        emailSender.sendAdminNotification(candidate, convocatoria);

        assertThat((String) modelFor(EmailType.ADMIN_CANDIDATE_RESPONDED).get("detailUrl"))
                .contains("/encuestas/" + convocatoria.getId())
                .doesNotContain("ranking");
    }

    @Test
    void adminNotificationLinksToTheRankingTabForCandidatesType() {
        Convocatoria convocatoria = convocatoria(FormType.CANDIDATES);
        Candidate candidate = candidate(convocatoria.getId());

        emailSender.sendAdminNotification(candidate, convocatoria);

        assertThat((String) modelFor(EmailType.ADMIN_CANDIDATE_RESPONDED).get("detailUrl"))
                .contains("/convocatorias/" + convocatoria.getId() + "?tab=ranking");
    }

    private void assertIsSurveyFlag(EmailType type, boolean expected) {
        assertThat(modelFor(type).get("isSurvey")).isEqualTo(expected);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> modelFor(EmailType type) {
        ArgumentCaptor<Map<String, Object>> modelCaptor = ArgumentCaptor.forClass(Map.class);
        org.mockito.Mockito.verify(sendEmail, org.mockito.Mockito.atLeastOnce())
                .send(eq(type), any(), modelCaptor.capture());
        return modelCaptor.getValue();
    }

    private Convocatoria convocatoria(FormType type) {
        return Convocatoria.builder().id(UUID.randomUUID()).tenantId(tenantId)
                .name("Test").type(type).build();
    }

    private Candidate candidate(UUID convocatoriaId) {
        return Candidate.builder().id(UUID.randomUUID()).convocatoriaId(convocatoriaId)
                .tenantId(tenantId).name("Ana").email("ana@test.com")
                .status(CandidateStatus.INVITED).token(UUID.randomUUID()).build();
    }
}
