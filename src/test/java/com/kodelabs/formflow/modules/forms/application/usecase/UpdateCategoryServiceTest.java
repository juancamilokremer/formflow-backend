package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.domain.model.Category;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateCategoryCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CategoryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCategoryServiceTest {

    @Mock private CategoryRepositoryPort categoryRepository;
    @InjectMocks private UpdateCategoryService service;

    private UUID id;
    private UUID tenantId;
    private Category existing;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        existing = Category.builder()
                .id(id).tenantId(tenantId)
                .name("Original").color("#000000").description("desc")
                .build();
    }

    @Test
    void updatesAllFieldsAndReturnsResult() {
        when(categoryRepository.findByIdAndTenantId(id, tenantId)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CategoryResult result = service.execute(
                new UpdateCategoryCommand(id, tenantId, "Actualizada", "#FF0000", "nueva desc"));

        assertThat(result.name()).isEqualTo("Actualizada");
        assertThat(result.color()).isEqualTo("#FF0000");
        assertThat(result.description()).isEqualTo("nueva desc");
    }

    @Test
    void allowsUpdateWithSameName() {
        when(categoryRepository.findByIdAndTenantId(id, tenantId)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute(new UpdateCategoryCommand(id, tenantId, "Original", "#FFFFFF", null));

        verify(categoryRepository, never()).existsByNameAndTenantId(any(), any());
    }

    @Test
    void throwsConflictWhenNewNameAlreadyTakenByAnotherCategory() {
        when(categoryRepository.findByIdAndTenantId(id, tenantId)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByNameAndTenantId("Tomado", tenantId)).thenReturn(true);

        assertThatThrownBy(() -> service.execute(
                new UpdateCategoryCommand(id, tenantId, "Tomado", "#000000", null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void throwsNotFoundWhenCategoryDoesNotExist() {
        when(categoryRepository.findByIdAndTenantId(id, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(
                new UpdateCategoryCommand(id, tenantId, "X", "#000000", null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
