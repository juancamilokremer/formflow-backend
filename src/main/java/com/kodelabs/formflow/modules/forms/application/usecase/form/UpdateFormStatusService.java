package com.kodelabs.formflow.modules.forms.application.usecase.form;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.port.in.UpdateFormStatusUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateFormStatusCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormSummaryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UpdateFormStatusService implements UpdateFormStatusUseCase {

    private final FormRepositoryPort formRepository;
    private final FormSectionRepositoryPort sectionRepository;
    private final FormResponseRepositoryPort responseRepository;

    @Override
    @Transactional
    public FormSummaryResult execute(UpdateFormStatusCommand command) {
        Form form = formRepository
                .findByIdAndTenantId(command.formId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.form.not_found", HttpStatus.NOT_FOUND,
                        command.formId().toString()));

        form.setStatus(command.status());
        form.setUpdatedBy(command.userId());

        Form saved = formRepository.save(form);
        int sectionCount = sectionRepository.countActiveByFormId(saved.getId());
        Map<java.util.UUID, Integer> responseCounts = responseRepository.countByFormIds(List.of(saved.getId()));
        Map<java.util.UUID, java.time.Instant> lastDates = responseRepository.lastResponseAtByFormIds(List.of(saved.getId()));

        return FormSummaryResult.of(
                saved,
                sectionCount,
                responseCounts.getOrDefault(saved.getId(), 0),
                lastDates.get(saved.getId()));
    }
}
