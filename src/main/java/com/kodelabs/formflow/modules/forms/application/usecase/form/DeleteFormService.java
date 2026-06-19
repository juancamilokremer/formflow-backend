package com.kodelabs.formflow.modules.forms.application.usecase.form;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.port.in.DeleteFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.DeleteFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteFormService implements DeleteFormUseCase {

    private final FormRepositoryPort formRepository;

    @Override
    @Transactional
    public void execute(DeleteFormCommand command) {
        Form form = formRepository
                .findByIdAndTenantId(command.formId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.form.not_found", HttpStatus.NOT_FOUND,
                        command.formId().toString()));

        form.softDelete();
        formRepository.save(form);
    }
}
