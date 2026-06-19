package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.domain.port.in.ListCategoriesUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ListCategoriesQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CategoryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListCategoriesService implements ListCategoriesUseCase {

    private final CategoryRepositoryPort categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResult> execute(ListCategoriesQuery query) {
        return categoryRepository.findAllByTenantId(query.tenantId()).stream()
                .map(CategoryResult::from)
                .toList();
    }
}
