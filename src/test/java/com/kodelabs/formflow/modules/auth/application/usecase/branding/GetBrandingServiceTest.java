package com.kodelabs.formflow.modules.auth.application.usecase.branding;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetBrandingQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.BrandingResult;
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
class GetBrandingServiceTest {

    @Mock private TenantRepositoryPort tenantRepository;
    @InjectMocks private GetBrandingService service;

    @Test
    void returnsTheBrandingFields() {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder().id(tenantId).name("Empresa ABC")
                .logoUrl("http://x/logo.png").primaryColor("#111111").secondaryColor("#222222").build();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        BrandingResult result = service.execute(new GetBrandingQuery(tenantId));

        assertThat(result.tenantName()).isEqualTo("Empresa ABC");
        assertThat(result.logoUrl()).isEqualTo("http://x/logo.png");
        assertThat(result.primaryColor()).isEqualTo("#111111");
        assertThat(result.faviconUrl()).isNull();
    }

    @Test
    void throwsWhenTenantDoesNotExist() {
        UUID tenantId = UUID.randomUUID();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new GetBrandingQuery(tenantId)))
                .isInstanceOf(BusinessException.class);
    }
}
