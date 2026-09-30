package com.kodelabs.formflow.modules.auth.application.usecase.tenant;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.port.in.ListAllTenantsUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ListTenantsQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantPageResult;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListAllTenantsService implements ListAllTenantsUseCase {

    private final TenantRepositoryPort tenantRepository;

    @Override
    public TenantPageResult execute(ListTenantsQuery query) {
        long total = tenantRepository.countAll(query.status(), query.plan());
        int totalPages = query.size() > 0 ? (int) Math.ceil((double) total / query.size()) : 0;

        var items = tenantRepository.findAll(query.page(), query.size(), query.status(), query.plan())
                .stream().map(TenantResult::from).toList();

        return new TenantPageResult(items, total, totalPages, query.page(), query.size());
    }
}
