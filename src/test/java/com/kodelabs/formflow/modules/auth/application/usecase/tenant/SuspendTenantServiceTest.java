package com.kodelabs.formflow.modules.auth.application.usecase.tenant;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.TenantStatus;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.SuspendTenantCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuspendTenantServiceTest {

    @Mock private TenantRepositoryPort tenantRepository;
    @InjectMocks private SuspendTenantService service;

    @Test
    void setsStatusToSuspended() {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder().id(tenantId).status(TenantStatus.ACTIVE).build();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(tenant)).thenReturn(tenant);

        TenantResult result = service.execute(new SuspendTenantCommand(tenantId));

        assertThat(result.status()).isEqualTo(TenantStatus.SUSPENDED);
    }

    @Test
    void throwsNotFoundWhenTenantDoesNotExist() {
        UUID tenantId = UUID.randomUUID();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new SuspendTenantCommand(tenantId)))
                .isInstanceOf(BusinessException.class);
    }
}
