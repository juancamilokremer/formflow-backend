package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.domain.model.Category;
import com.kodelabs.formflow.modules.forms.domain.port.in.UpdateCategoryUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateCategoryCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CategoryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateCategoryService implements UpdateCategoryUseCase {

    private final CategoryRepositoryPort categoryRepository;

    @Override
    @Transactional
    public CategoryResult execute(UpdateCategoryCommand command) {
        Category category = categoryRepository
                .findByIdAndTenantId(command.id(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.category.not_found",
                        HttpStatus.NOT_FOUND, command.id().toString()));

        if (!category.getName().equals(command.name()) &&
                categoryRepository.existsByNameAndTenantId(command.name(), command.tenantId())) {
            throw new BusinessException("error.category.name_already_exists",
                    HttpStatus.CONFLICT, command.name());
        }

        category.setName(command.name());
        category.setColor(command.color());
        category.setDescription(command.description());

        return CategoryResult.from(categoryRepository.save(category));
    }
}
