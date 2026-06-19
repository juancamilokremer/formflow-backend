package com.kodelabs.formflow.modules.forms.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.forms.domain.model.Category;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper.CategoryPersistenceMapper;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository.CategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepositoryPort {

    private final CategoryJpaRepository categoryJpa;
    private final CategoryPersistenceMapper categoryMapper;

    @Override
    public Category save(Category category) {
        return categoryMapper.toDomain(categoryJpa.save(categoryMapper.toEntity(category)));
    }

    @Override
    public Optional<Category> findByIdAndTenantId(UUID id, UUID tenantId) {
        return categoryJpa.findByIdAndTenantId(id, tenantId).map(categoryMapper::toDomain);
    }

    @Override
    public List<Category> findAllByTenantId(UUID tenantId) {
        return categoryJpa.findAllByTenantIdOrderByNameAsc(tenantId).stream()
                .map(categoryMapper::toDomain).toList();
    }

    @Override
    public List<Category> findAllByIdsAndTenantId(List<UUID> ids, UUID tenantId) {
        if (ids.isEmpty()) return List.of();
        return categoryJpa.findAllByIdsAndTenantId(ids, tenantId).stream()
                .map(categoryMapper::toDomain).toList();
    }

    @Override
    public boolean existsByIdAndTenantId(UUID id, UUID tenantId) {
        return categoryJpa.existsByIdAndTenantId(id, tenantId);
    }

    @Override
    public boolean existsByNameAndTenantId(String name, UUID tenantId) {
        return categoryJpa.existsByNameAndTenantId(name, tenantId);
    }

    @Override
    public void deleteById(UUID id) {
        categoryJpa.deleteById(id);
    }
}
