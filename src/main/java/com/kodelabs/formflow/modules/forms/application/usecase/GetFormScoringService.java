package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.domain.model.Category;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetFormScoringUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetFormScoringQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CategoryScoringResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormScoringResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetFormScoringService implements GetFormScoringUseCase {

    private final FormRepositoryPort formRepository;
    private final FormSectionRepositoryPort sectionRepository;
    private final FormQuestionRepositoryPort questionRepository;
    private final CategoryRepositoryPort categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public FormScoringResult execute(GetFormScoringQuery query) {
        Form form = formRepository
                .findByIdAndTenantId(query.formId(), query.tenantId())
                .orElseThrow(() -> new BusinessException("error.form.not_found",
                        HttpStatus.NOT_FOUND, query.formId().toString()));

        List<UUID> sectionIds = sectionRepository
                .findActiveSectionIdsByFormId(form.getId());

        List<FormQuestion> questions = sectionIds.isEmpty()
                ? List.of()
                : questionRepository.findAllActiveBySectionIds(sectionIds).values().stream()
                        .flatMap(List::stream)
                        .toList();

        Map<UUID, List<FormQuestion>> byCategory = questions.stream()
                .filter(q -> q.getCategoryId() != null)
                .collect(Collectors.groupingBy(FormQuestion::getCategoryId));

        List<UUID> categoryIds = new ArrayList<>(byCategory.keySet());
        Map<UUID, Category> categoryMap = categoryRepository
                .findAllByIdsAndTenantId(categoryIds, query.tenantId()).stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        List<CategoryScoringResult> categories = byCategory.entrySet().stream()
                .map(entry -> {
                    UUID catId = entry.getKey();
                    List<FormQuestion> catQuestions = entry.getValue();
                    int maxScore = catQuestions.stream()
                            .mapToInt(q -> q.getConfig() != null ? q.getConfig().maxScore() : 0)
                            .sum();
                    Category cat = categoryMap.get(catId);
                    String name = cat != null ? cat.getName() : "Sin categoría";
                    String color = cat != null ? cat.getColor() : null;
                    return new CategoryScoringResult(catId, name, color, catQuestions.size(), maxScore);
                })
                .sorted((a, b) -> a.categoryName().compareToIgnoreCase(b.categoryName()))
                .toList();

        int totalMaxScore = categories.stream().mapToInt(CategoryScoringResult::maxScore).sum();

        return new FormScoringResult(form.getId(), totalMaxScore, categories);
    }
}
