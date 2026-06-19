package com.kodelabs.formflow.modules.forms.application.usecase.form;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetFormQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormDetailResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetFormService implements GetFormUseCase {

    private final FormRepositoryPort formRepository;

    @Override
    @Transactional(readOnly = true)
    public FormDetailResult execute(GetFormQuery query) {
        Form form = formRepository
                .findByIdAndTenantIdWithSections(query.formId(), query.tenantId())
                .orElseThrow(() -> new BusinessException("error.form.not_found", HttpStatus.NOT_FOUND,
                        query.formId().toString()));
        return new FormDetailResult(form);
    }
}
