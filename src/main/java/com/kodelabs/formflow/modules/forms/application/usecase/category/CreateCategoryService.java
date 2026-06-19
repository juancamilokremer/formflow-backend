package com.kodelabs.formflow.modules.forms.application.usecase.category;

import com.kodelabs.formflow.modules.forms.domain.model.Category;
import com.kodelabs.formflow.modules.forms.domain.port.in.CreateCategoryUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.CreateCategoryCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CategoryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCategoryService implements CreateCategoryUseCase {

    private final CategoryRepositoryPort categoryRepository;

    @Override
    @Transactional
    public CategoryResult execute(CreateCategoryCommand command) {
        if (categoryRepository.existsByNameAndTenantId(command.name(), command.tenantId())) {
            throw new BusinessException("error.category.name_already_exists",
                    HttpStatus.CONFLICT, command.name());
        }

        Category category = Category.builder()
                .tenantId(command.tenantId())
                .name(command.name())
                .color(command.color())
                .description(command.description())
                .build();

        return CategoryResult.from(categoryRepository.save(category));
    }
}
