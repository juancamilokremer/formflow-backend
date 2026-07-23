package com.kodelabs.formflow.modules.forms.application.usecase.form;

import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetFormVersionHistoryUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetFormVersionHistoryQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormVersionResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetFormVersionHistoryService implements GetFormVersionHistoryUseCase {

    private final FormLoader formLoader;
    private final FormRepositoryPort formRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FormVersionResult> execute(GetFormVersionHistoryQuery query) {
        Form form = formLoader.loadOrThrow(query.formId(), query.tenantId());
        UUID effectiveRoot = form.getRootFormId() != null ? form.getRootFormId() : form.getId();

        return formRepository.findFamilyByRootId(effectiveRoot, query.tenantId()).stream()
                .map(FormVersionResult::from)
                .toList();
    }
}
