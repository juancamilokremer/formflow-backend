package com.kodelabs.formflow.modules.auth.application.usecase.user;

import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ChangeUserRoleCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.UserResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeUserRoleServiceTest {

    @Mock private UserRepositoryPort userRepository;
    @InjectMocks private ChangeUserRoleService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    void changesTheRoleWhenNotDemotingTheOnlyAdmin() {
        User user = User.builder().id(userId).tenantId(tenantId).role(UserRole.EDITOR).build();
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResult result = service.execute(new ChangeUserRoleCommand(tenantId, userId, UserRole.TENANT_ADMIN));

        assertThat(result.role()).isEqualTo(UserRole.TENANT_ADMIN);
    }

    @Test
    void rejectsDemotingTheOnlyActiveAdmin() {
        User user = User.builder().id(userId).tenantId(tenantId).role(UserRole.TENANT_ADMIN).build();
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(userRepository.countByTenantIdAndRoleAndActiveTrue(tenantId, UserRole.TENANT_ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.execute(new ChangeUserRoleCommand(tenantId, userId, UserRole.EDITOR)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.user.must_keep_one_admin")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void allowsDemotingAnAdminWhenAnotherOneRemains() {
        User user = User.builder().id(userId).tenantId(tenantId).role(UserRole.TENANT_ADMIN).build();
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(userRepository.countByTenantIdAndRoleAndActiveTrue(tenantId, UserRole.TENANT_ADMIN)).thenReturn(2L);
        when(userRepository.save(user)).thenReturn(user);

        UserResult result = service.execute(new ChangeUserRoleCommand(tenantId, userId, UserRole.VIEWER));

        assertThat(result.role()).isEqualTo(UserRole.VIEWER);
    }

    @Test
    void rejectsChangingRoleToSuperAdmin() {
        User user = User.builder().id(userId).tenantId(tenantId).role(UserRole.EDITOR).build();
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.execute(new ChangeUserRoleCommand(tenantId, userId, UserRole.SUPER_ADMIN)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.user.invalid_role");
    }

    @Test
    void rejectsChangingRoleOfASuperAdmin() {
        User user = User.builder().id(userId).tenantId(tenantId).role(UserRole.SUPER_ADMIN).build();
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.execute(new ChangeUserRoleCommand(tenantId, userId, UserRole.EDITOR)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.user.invalid_role");
    }
}
