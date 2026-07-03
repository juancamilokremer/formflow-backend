package com.kodelabs.formflow.modules.auth.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.entity.TenantJpaEntity;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.repository.TenantJpaRepository;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.repository.UserJpaRepository;
import com.kodelabs.formflow.modules.forms.domain.model.TenantInfo;
import com.kodelabs.formflow.modules.forms.domain.port.out.TenantInfoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TenantInfoAdapter implements TenantInfoPort {

    private final TenantJpaRepository tenantRepository;
    private final UserJpaRepository userRepository;

    @Override
    public Optional<TenantInfo> findByTenantId(UUID tenantId) {
        Optional<TenantJpaEntity> tenant = tenantRepository.findById(tenantId);
        if (tenant.isEmpty()) return Optional.empty();

        String adminEmail = userRepository
                .findFirstByTenantIdAndRole(tenantId, UserRole.TENANT_ADMIN)
                .map(UserJpaEntity::getEmail)
                .orElse(null);

        return Optional.of(new TenantInfo(tenant.get().getName(), adminEmail));
    }
}
