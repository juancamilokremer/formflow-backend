package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.usecase.category.CreateCategoryService;
import com.kodelabs.formflow.modules.forms.domain.model.Category;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.CreateCategoryCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CategoryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCategoryServiceTest {

    @Mock private CategoryRepositoryPort categoryRepository;
    @InjectMocks private CreateCategoryService service;

    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
    }

    @Test
    void savesAndReturnsCategoryWithGivenFields() {
        when(categoryRepository.existsByNameAndTenantId("Técnicas", tenantId)).thenReturn(false);
        when(categoryRepository.save(any())).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        CategoryResult result = service.execute(
                new CreateCategoryCommand(tenantId, "Técnicas", "#FF5733", "Competencias técnicas"));

        assertThat(result.name()).isEqualTo("Técnicas");
        assertThat(result.color()).isEqualTo("#FF5733");
        assertThat(result.description()).isEqualTo("Competencias técnicas");
    }

    @Test
    void persistsCategoryWithCorrectTenantId() {
        when(categoryRepository.existsByNameAndTenantId(any(), any())).thenReturn(false);
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute(new CreateCategoryCommand(tenantId, "Blandas", "#6B7280", null));

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertThat(captor.getValue().getTenantId()).isEqualTo(tenantId);
    }

    @Test
    void throwsConflictWhenNameAlreadyExistsForTenant() {
        when(categoryRepository.existsByNameAndTenantId("Técnicas", tenantId)).thenReturn(true);

        assertThatThrownBy(() -> service.execute(
                new CreateCategoryCommand(tenantId, "Técnicas", "#FF5733", null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        verify(categoryRepository, never()).save(any());
    }
}
