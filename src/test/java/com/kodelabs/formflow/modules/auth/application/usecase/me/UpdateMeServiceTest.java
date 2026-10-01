package com.kodelabs.formflow.modules.auth.application.usecase.me;

import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.UpdateMeCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.MeResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
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
class UpdateMeServiceTest {

    @Mock private UserRepositoryPort userRepository;
    @InjectMocks private UpdateMeService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    void updatesFirstAndLastName() {
        User user = User.builder().id(userId).tenantId(tenantId).firstName("Old").lastName("Name").build();
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        MeResult result = service.execute(new UpdateMeCommand(userId, tenantId, "Ada", "QA"));

        assertThat(result.firstName()).isEqualTo("Ada");
        assertThat(result.lastName()).isEqualTo("QA");
        assertThat(user.getFirstName()).isEqualTo("Ada");
    }

    @Test
    void rejectsWhenUserNotFound() {
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new UpdateMeCommand(userId, tenantId, "Ada", "QA")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.user.not_found");
    }
}
