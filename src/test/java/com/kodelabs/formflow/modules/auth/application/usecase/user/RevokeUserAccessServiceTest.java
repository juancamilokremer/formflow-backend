package com.kodelabs.formflow.modules.auth.application.usecase.user;

import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.RevokeUserAccessCommand;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevokeUserAccessServiceTest {

    @Mock private UserRepositoryPort userRepository;
    @InjectMocks private RevokeUserAccessService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID actingUserId = UUID.randomUUID();
    private final UUID targetUserId = UUID.randomUUID();

    @Test
    void deactivatesTheTargetUser() {
        User target = User.builder().id(targetUserId).tenantId(tenantId).role(UserRole.EDITOR).active(true).build();
        when(userRepository.findByIdAndTenantId(targetUserId, tenantId)).thenReturn(Optional.of(target));
        when(userRepository.save(target)).thenReturn(target);

        service.execute(new RevokeUserAccessCommand(tenantId, actingUserId, targetUserId));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    @Test
    void rejectsRevokingOwnAccess() {
        assertThatThrownBy(() -> service.execute(new RevokeUserAccessCommand(tenantId, actingUserId, actingUserId)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.user.cannot_revoke_self")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsRevokingTheOnlyActiveAdmin() {
        User target = User.builder().id(targetUserId).tenantId(tenantId).role(UserRole.TENANT_ADMIN).active(true).build();
        when(userRepository.findByIdAndTenantId(targetUserId, tenantId)).thenReturn(Optional.of(target));
        when(userRepository.countByTenantIdAndRoleAndActiveTrue(tenantId, UserRole.TENANT_ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.execute(new RevokeUserAccessCommand(tenantId, actingUserId, targetUserId)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.user.must_keep_one_admin");
    }

    @Test
    void rejectsRevokingASuperAdmin() {
        User target = User.builder().id(targetUserId).tenantId(tenantId).role(UserRole.SUPER_ADMIN).active(true).build();
        when(userRepository.findByIdAndTenantId(targetUserId, tenantId)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> service.execute(new RevokeUserAccessCommand(tenantId, actingUserId, targetUserId)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.user.invalid_role");
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
