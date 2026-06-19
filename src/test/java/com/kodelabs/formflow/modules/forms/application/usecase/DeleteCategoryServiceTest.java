package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.usecase.category.DeleteCategoryService;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.DeleteCategoryCommand;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteCategoryServiceTest {

    @Mock private CategoryRepositoryPort categoryRepository;
    @Mock private FormQuestionRepositoryPort questionRepository;
    @InjectMocks private DeleteCategoryService service;

    private UUID id;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        tenantId = UUID.randomUUID();
    }

    @Test
    void deletesWhenCategoryExistsAndHasNoQuestions() {
        when(categoryRepository.existsByIdAndTenantId(id, tenantId)).thenReturn(true);
        when(questionRepository.existsActiveByCategoryIdAndTenantId(id, tenantId)).thenReturn(false);

        service.execute(new DeleteCategoryCommand(id, tenantId));

        verify(categoryRepository).deleteById(id);
    }

    @Test
    void throwsNotFoundWhenCategoryDoesNotExist() {
        when(categoryRepository.existsByIdAndTenantId(id, tenantId)).thenReturn(false);

        assertThatThrownBy(() -> service.execute(new DeleteCategoryCommand(id, tenantId)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(categoryRepository, never()).deleteById(id);
    }

    @Test
    void throwsConflictWhenCategoryHasAssignedQuestions() {
        when(categoryRepository.existsByIdAndTenantId(id, tenantId)).thenReturn(true);
        when(questionRepository.existsActiveByCategoryIdAndTenantId(id, tenantId)).thenReturn(true);

        assertThatThrownBy(() -> service.execute(new DeleteCategoryCommand(id, tenantId)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        verify(categoryRepository, never()).deleteById(id);
    }
}
