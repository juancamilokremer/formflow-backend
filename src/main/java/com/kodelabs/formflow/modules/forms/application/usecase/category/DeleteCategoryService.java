package com.kodelabs.formflow.modules.forms.application.usecase.category;

import com.kodelabs.formflow.modules.forms.domain.port.in.DeleteCategoryUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.DeleteCategoryCommand;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteCategoryService implements DeleteCategoryUseCase {

    private final CategoryRepositoryPort categoryRepository;
    private final FormQuestionRepositoryPort questionRepository;

    @Override
    @Transactional
    public void execute(DeleteCategoryCommand command) {
        if (!categoryRepository.existsByIdAndTenantId(command.id(), command.tenantId())) {
            throw new BusinessException("error.category.not_found",
                    HttpStatus.NOT_FOUND, command.id().toString());
        }

        if (questionRepository.existsActiveByCategoryIdAndTenantId(command.id(), command.tenantId())) {
            throw new BusinessException("error.category.has_questions",
                    HttpStatus.CONFLICT, command.id().toString());
        }

        categoryRepository.deleteById(command.id());
    }
}
