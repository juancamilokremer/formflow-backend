package com.kodelabs.formflow.modules.forms.application.usecase.form;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.port.in.ListFormsUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ListFormsQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormSummaryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListFormsService implements ListFormsUseCase {

    private final FormRepositoryPort formRepository;
    private final FormSectionRepositoryPort sectionRepository;
    private final FormResponseRepositoryPort responseRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FormSummaryResult> execute(ListFormsQuery query) {
        List<Form> forms = formRepository.findAllByTenantId(query.tenantId());
        if (forms.isEmpty()) return List.of();

        List<UUID> formIds = forms.stream().map(Form::getId).toList();
        Map<UUID, Integer> sectionCounts = sectionRepository.countAllActiveByFormIds(formIds);
        Map<UUID, Integer> responseCounts = responseRepository.countByFormIds(formIds);
        Map<UUID, Instant> lastResponseDates = responseRepository.lastResponseAtByFormIds(formIds);

        return forms.stream()
                .map(form -> FormSummaryResult.of(
                        form,
                        sectionCounts.getOrDefault(form.getId(), 0),
                        responseCounts.getOrDefault(form.getId(), 0),
                        lastResponseDates.get(form.getId())))
                .toList();
    }
}
