package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.domain.port.in.ListFormsUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ListFormsQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormSummaryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListFormsService implements ListFormsUseCase {

    private final FormRepositoryPort formRepository;
    private final FormSectionRepositoryPort sectionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FormSummaryResult> execute(ListFormsQuery query) {
        return formRepository.findAllByTenantId(query.tenantId()).stream()
                .map(form -> FormSummaryResult.of(
                        form,
                        sectionRepository.countActiveByFormId(form.getId())))
                .toList();
    }
}
