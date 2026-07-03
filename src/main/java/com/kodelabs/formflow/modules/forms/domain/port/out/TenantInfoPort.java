package com.kodelabs.formflow.modules.forms.domain.port.out;

import com.kodelabs.formflow.modules.forms.domain.model.TenantInfo;

import java.util.Optional;
import java.util.UUID;

public interface TenantInfoPort {

    Optional<TenantInfo> findByTenantId(UUID tenantId);
}
