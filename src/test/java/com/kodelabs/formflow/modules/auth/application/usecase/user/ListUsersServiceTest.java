package com.kodelabs.formflow.modules.auth.application.usecase.user;

import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ListUsersQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.UserResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListUsersServiceTest {

    @Mock private UserRepositoryPort userRepository;
    @InjectMocks private ListUsersService service;

    @Test
    void returnsTheActiveUsersOfTheTenant() {
        UUID tenantId = UUID.randomUUID();
        User user = User.builder().id(UUID.randomUUID()).tenantId(tenantId)
                .email("a@b.com").firstName("Ana").lastName("Gomez").role(UserRole.VIEWER).active(true).build();
        when(userRepository.findAllByTenantIdAndActiveTrue(tenantId)).thenReturn(List.of(user));

        List<UserResult> results = service.execute(new ListUsersQuery(tenantId));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).email()).isEqualTo("a@b.com");
    }
}
