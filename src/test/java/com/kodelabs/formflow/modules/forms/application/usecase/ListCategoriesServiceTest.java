package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.usecase.category.ListCategoriesService;
import com.kodelabs.formflow.modules.forms.domain.model.Category;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ListCategoriesQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CategoryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListCategoriesServiceTest {

    @Mock private CategoryRepositoryPort categoryRepository;
    @InjectMocks private ListCategoriesService service;

    @Test
    void returnsAllCategoriesForTenant() {
        UUID tenantId = UUID.randomUUID();
        List<Category> categories = List.of(
                Category.builder().id(UUID.randomUUID()).tenantId(tenantId).name("Técnicas").color("#FF5733").build(),
                Category.builder().id(UUID.randomUUID()).tenantId(tenantId).name("Blandas").color("#6B7280").build());
        when(categoryRepository.findAllByTenantId(tenantId)).thenReturn(categories);

        List<CategoryResult> results = service.execute(new ListCategoriesQuery(tenantId));

        assertThat(results).hasSize(2);
        assertThat(results.get(0).name()).isEqualTo("Técnicas");
        assertThat(results.get(1).name()).isEqualTo("Blandas");
    }

    @Test
    void returnsEmptyListWhenNoCategoriesExist() {
        UUID tenantId = UUID.randomUUID();
        when(categoryRepository.findAllByTenantId(tenantId)).thenReturn(List.of());

        List<CategoryResult> results = service.execute(new ListCategoriesQuery(tenantId));

        assertThat(results).isEmpty();
    }
}
