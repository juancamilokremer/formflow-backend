package com.kodelabs.formflow.modules.auth.application.usecase.tenant;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.UpdateTenantCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import com.kodelabs.formflow.shared.sanitize.HtmlSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateTenantServiceTest {

    @Mock private TenantRepositoryPort tenantRepository;
    @Spy private HtmlSanitizer htmlSanitizer = new HtmlSanitizer();
    @InjectMocks private UpdateTenantService service;

    private final UUID tenantId = UUID.randomUUID();
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = Tenant.builder().id(tenantId).slug("empresa-abc").name("Old name").build();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
    }

    @Test
    void updatesNameAndBrandingFields() {
        when(tenantRepository.save(tenant)).thenReturn(tenant);
        var command = new UpdateTenantCommand(tenantId, "New name", "logo.png", "#111111", "#222222", null);

        TenantResult result = service.execute(command);

        assertThat(result.name()).isEqualTo("New name");
        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository).save(captor.capture());
        assertThat(captor.getValue().getLogoUrl()).isEqualTo("logo.png");
        assertThat(captor.getValue().getPrimaryColor()).isEqualTo("#111111");
    }

    @Test
    void acceptsTheSameSlugAsNoOp() {
        when(tenantRepository.save(tenant)).thenReturn(tenant);
        var command = new UpdateTenantCommand(tenantId, "New name", null, null, null, "empresa-abc");

        TenantResult result = service.execute(command);

        assertThat(result.name()).isEqualTo("New name");
    }

    @Test
    void rejectsAnAttemptToChangeTheSlug() {
        var command = new UpdateTenantCommand(tenantId, "New name", null, null, null, "otro-slug");

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.tenant.slug_immutable")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }
}
