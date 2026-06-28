package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.usecase.category.GetCategoryService;
import com.kodelabs.formflow.modules.forms.domain.model.Category;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetCategoryQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CategoryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCategoryServiceTest {

    @Mock private CategoryRepositoryPort categoryRepository;
    @InjectMocks private GetCategoryService service;

    @Test
    void returnsCategoryResultWhenFound() {
        UUID id = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Category category = Category.builder().id(id).tenantId(tenantId)
                .name("Técnicas").color("#FF5733").description("Competencias técnicas").build();
        when(categoryRepository.findByIdAndTenantId(id, tenantId)).thenReturn(Optional.of(category));

        CategoryResult result = service.execute(new GetCategoryQuery(id, tenantId));

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("Técnicas");
        assertThat(result.color()).isEqualTo("#FF5733");
    }

    @Test
    void throwsNotFoundWhenCategoryDoesNotBelongToTenant() {
        UUID id = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        when(categoryRepository.findByIdAndTenantId(id, tenantId)).thenReturn(Optional.empty());

        var query = new GetCategoryQuery(id, tenantId);
        assertThatThrownBy(() -> service.execute(query))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
