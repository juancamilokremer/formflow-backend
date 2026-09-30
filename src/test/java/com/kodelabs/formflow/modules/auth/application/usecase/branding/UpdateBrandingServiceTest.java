package com.kodelabs.formflow.modules.auth.application.usecase.branding;

import com.kodelabs.formflow.modules.auth.application.service.BrandingCache;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.UpdateBrandingCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.BrandingResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateBrandingServiceTest {

    @Mock private TenantRepositoryPort tenantRepository;
    @Mock private BrandingCache brandingCache;
    @InjectMocks private UpdateBrandingService service;

    @Test
    void updatesNameAndColorsButNeverTheLogo() {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder().id(tenantId).slug("empresa-abc")
                .name("Old").logoUrl("http://x/old-logo.png").build();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(tenant)).thenReturn(tenant);

        BrandingResult result = service.execute(
                new UpdateBrandingCommand(tenantId, "New name", "#111111", "#222222"));

        assertThat(result.tenantName()).isEqualTo("New name");
        assertThat(result.logoUrl()).isEqualTo("http://x/old-logo.png");
        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository).save(captor.capture());
        assertThat(captor.getValue().getLogoUrl()).isEqualTo("http://x/old-logo.png");
        verify(brandingCache).invalidate("empresa-abc");
    }
}
