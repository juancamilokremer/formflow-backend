package com.kodelabs.formflow.modules.forms.application.usecase.category;

import com.kodelabs.formflow.modules.forms.domain.model.Category;
import com.kodelabs.formflow.modules.forms.domain.port.in.UpdateCategoryUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateCategoryCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CategoryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import com.kodelabs.formflow.shared.sanitize.HtmlSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateCategoryService implements UpdateCategoryUseCase {

    private final CategoryRepositoryPort categoryRepository;
    private final HtmlSanitizer htmlSanitizer;

    @Override
    @Transactional
    public CategoryResult execute(UpdateCategoryCommand command) {
        Category category = categoryRepository
                .findByIdAndTenantId(command.id(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.category.not_found",
                        HttpStatus.NOT_FOUND, command.id().toString()));

        String name = htmlSanitizer.sanitize(command.name());
        if (!category.getName().equals(name) &&
                categoryRepository.existsByNameAndTenantId(name, command.tenantId())) {
            throw new BusinessException("error.category.name_already_exists",
                    HttpStatus.CONFLICT, name);
        }

        category.setName(name);
        category.setColor(command.color());
        category.setDescription(htmlSanitizer.sanitize(command.description()));

        return CategoryResult.from(categoryRepository.save(category));
    }
}
