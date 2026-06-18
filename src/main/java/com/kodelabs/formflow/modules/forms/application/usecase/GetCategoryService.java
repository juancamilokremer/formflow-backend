package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.domain.port.in.GetCategoryUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetCategoryQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CategoryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCategoryService implements GetCategoryUseCase {

    private final CategoryRepositoryPort categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public CategoryResult execute(GetCategoryQuery query) {
        return categoryRepository.findByIdAndTenantId(query.id(), query.tenantId())
                .map(CategoryResult::from)
                .orElseThrow(() -> new BusinessException("error.category.not_found",
                        HttpStatus.NOT_FOUND, query.id().toString()));
    }
}
