package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateResponseSubmittedEvent;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConvocatoriaEmailListener {

    private final ConvocatoriaEmailSender emailSender;
    private final CandidateRepositoryPort candidateRepository;
    private final ConvocatoriaRepositoryPort convocatoriaRepository;

    @Async("emailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCandidateResponded(CandidateResponseSubmittedEvent event) {
        try {
            Candidate candidate = candidateRepository
                    .findByIdAndConvocatoriaId(event.candidateId(), event.convocatoriaId())
                    .orElse(null);
            if (candidate == null) {
                log.warn("Candidate {} not found, skipping post-response emails", event.candidateId());
                return;
            }
            Convocatoria convocatoria = convocatoriaRepository
                    .findByIdAndTenantId(event.convocatoriaId(), event.tenantId())
                    .orElse(null);
            if (convocatoria == null) {
                log.warn("Convocatoria {} not found, skipping post-response emails", event.convocatoriaId());
                return;
            }
            emailSender.sendResponseConfirmation(candidate, convocatoria);
            emailSender.sendAdminNotification(candidate, convocatoria);
        } catch (Exception ex) {
            log.error("Failed to send post-response emails for candidate {}: {}",
                    event.candidateId(), ex.getMessage(), ex);
        }
    }
}
