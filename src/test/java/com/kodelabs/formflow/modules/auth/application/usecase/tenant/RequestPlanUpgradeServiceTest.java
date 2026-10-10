package com.kodelabs.formflow.modules.auth.application.usecase.tenant;

import com.kodelabs.formflow.modules.auth.application.service.PlanUpgradeRequestEmailSender;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.RequestPlanUpgradeCommand;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestPlanUpgradeServiceTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private TenantRepositoryPort tenantRepository;
    @Mock private PlanUpgradeRequestEmailSender emailSender;
    @InjectMocks private RequestPlanUpgradeService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID tenantId = UUID.randomUUID();
    private User user;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        user = User.builder().id(userId).tenantId(tenantId).email("admin@abc.com").firstName("Juan").lastName("Perez").build();
        tenant = Tenant.builder().id(tenantId).name("Empresa ABC").plan(TenantPlan.FREE).build();
    }

    @Test
    void sendsTheRequestWithTheResolvedUserAndTenant() {
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        var command = new RequestPlanUpgradeCommand(userId, tenantId, TenantPlan.STARTER, "Queremos más forms");

        service.execute(command);

        verify(emailSender).send(eq(user), eq(tenant), eq(TenantPlan.STARTER), eq("Queremos más forms"));
    }

    @Test
    void rejectsAnUnknownUser() {
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.empty());
        var command = new RequestPlanUpgradeCommand(userId, tenantId, TenantPlan.STARTER, null);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.user.not_found");
    }

    @Test
    void rejectsAnUnknownTenant() {
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());
        var command = new RequestPlanUpgradeCommand(userId, tenantId, TenantPlan.STARTER, null);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.tenant.not_found");
    }

    @Test
    void acceptsANullMessage() {
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        var command = new RequestPlanUpgradeCommand(userId, tenantId, TenantPlan.PRO, null);

        service.execute(command);

        verify(emailSender).send(eq(user), eq(tenant), eq(TenantPlan.PRO), eq((String) null));
    }
}
