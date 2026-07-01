package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.ConvocatoriaEmailSender;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.port.in.SendConvocatoriaRemindersUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.SendConvocatoriaRemindersCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SendConvocatoriaRemindersResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SendConvocatoriaRemindersService implements SendConvocatoriaRemindersUseCase {

    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final CandidateRepositoryPort candidateRepository;
    private final ConvocatoriaEmailSender emailSender;

    @Override
    public SendConvocatoriaRemindersResult execute(SendConvocatoriaRemindersCommand command) {
        Convocatoria convocatoria = convocatoriaRepository
                .findByIdAndTenantId(command.convocatoriaId(), command.tenantId())
                .orElseThrow(() -> new BusinessException(
                        "error.convocatoria.not_found", HttpStatus.NOT_FOUND, command.convocatoriaId()));
        if (!convocatoria.isActive()) {
            throw new BusinessException("error.convocatoria.not_active", HttpStatus.CONFLICT);
        }
        List<Candidate> pending = candidateRepository
                .findAllByConvocatoriaId(convocatoria.getId())
                .stream()
                .filter(c -> c.getStatus() == CandidateStatus.INVITED)
                .toList();
        pending.forEach(c -> emailSender.sendReminder(c, convocatoria));
        return new SendConvocatoriaRemindersResult(pending.size());
    }
}
