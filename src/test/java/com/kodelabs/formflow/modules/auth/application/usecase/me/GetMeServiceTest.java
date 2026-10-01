package com.kodelabs.formflow.modules.auth.application.usecase.me;

import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetMeQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.MeResult;
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
class GetMeServiceTest {

    @Mock private UserRepositoryPort userRepository;
    @InjectMocks private GetMeService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    void returnsTheCurrentUsersData() {
        User user = User.builder().id(userId).tenantId(tenantId)
                .email("ada@empresa.com").firstName("Ada").lastName("QA")
                .role(UserRole.EDITOR).avatarUrl("http://x/avatar.png").build();
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));

        MeResult result = service.execute(new GetMeQuery(userId, tenantId));

        assertThat(result.email()).isEqualTo("ada@empresa.com");
        assertThat(result.avatarUrl()).isEqualTo("http://x/avatar.png");
        assertThat(result.role()).isEqualTo(UserRole.EDITOR);
    }

    @Test
    void rejectsWhenUserNotFound() {
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new GetMeQuery(userId, tenantId)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.user.not_found")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
