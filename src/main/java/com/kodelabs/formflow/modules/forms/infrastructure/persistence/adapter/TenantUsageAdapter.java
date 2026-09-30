package com.kodelabs.formflow.modules.forms.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.auth.domain.port.out.TenantUsagePort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TenantUsageAdapter implements TenantUsagePort {

    private final FormRepositoryPort formRepository;
    private final FormResponseRepositoryPort responseRepository;

    @Override
    public long countForms(UUID tenantId) {
        return formRepository.findAllByTenantId(tenantId).size();
    }

    @Override
    public long countResponsesThisMonth(UUID tenantId, Instant from, Instant to) {
        return responseRepository.countByTenantId(tenantId, from, to);
    }

    @Override
    public long countAllResponsesThisMonth(Instant from, Instant to) {
        return responseRepository.countAllSince(from, to);
    }
}
