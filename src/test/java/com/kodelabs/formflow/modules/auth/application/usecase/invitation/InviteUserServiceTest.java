package com.kodelabs.formflow.modules.auth.application.usecase.invitation;

import com.kodelabs.formflow.modules.auth.domain.model.InvitationStatus;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.UserInvitation;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.InviteUserCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.InvitationResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TokenServicePort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserInvitationRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.modules.notifications.domain.port.in.SendEmailUseCase;
import com.kodelabs.formflow.shared.exception.BusinessException;
import com.kodelabs.formflow.shared.i18n.Messages;
import com.kodelabs.formflow.shared.planlimit.PlanLimitExceededException;
import com.kodelabs.formflow.shared.planlimit.PlanLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InviteUserServiceTest {

    @Mock private TenantRepositoryPort tenantRepository;
    @Mock private UserRepositoryPort userRepository;
    @Mock private UserInvitationRepositoryPort invitationRepository;
    @Mock private PlanLimitService planLimitService;
    @Mock private TokenServicePort tokenService;
    @Mock private SendEmailUseCase sendEmail;
    @Mock private Messages messages;

    private InviteUserService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID invitedByUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new InviteUserService(tenantRepository, userRepository, invitationRepository,
                planLimitService, tokenService, sendEmail, messages);
        ReflectionTestUtils.setField(service, "frontendBaseUrl", "http://localhost:4200");

        lenient().when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(Tenant.builder().id(tenantId).name("Empresa ABC").build()));
        lenient().when(tokenService.generateOpaqueToken()).thenReturn("raw-token");
        lenient().when(tokenService.hashToken("raw-token")).thenReturn("hashed-token");
        lenient().when(invitationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createsTheInvitationAndSendsTheEmail() {
        var command = new InviteUserCommand(tenantId, invitedByUserId, "nuevo@empresa.com", UserRole.EDITOR);

        InvitationResult result = service.execute(command);

        assertThat(result.email()).isEqualTo("nuevo@empresa.com");
        assertThat(result.role()).isEqualTo(UserRole.EDITOR);
        verify(sendEmail).send(any(), org.mockito.ArgumentMatchers.eq("nuevo@empresa.com"), any());
    }

    @Test
    void rejectsWhenThePlanLimitIsReached() {
        doThrow(new PlanLimitExceededException("error.plan_limit.users", 1,
                        com.kodelabs.formflow.modules.auth.domain.model.TenantPlan.FREE,
                        com.kodelabs.formflow.modules.auth.domain.model.TenantPlan.STARTER))
                .when(planLimitService).checkUserLimit(tenantId);

        var command = new InviteUserCommand(tenantId, invitedByUserId, "nuevo@empresa.com", UserRole.EDITOR);

        assertThatThrownBy(() -> service.execute(command)).isInstanceOf(PlanLimitExceededException.class);
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void rejectsWhenTheEmailAlreadyHasAnActiveAccount() {
        when(userRepository.existsByEmailAndTenantId("ya-existe@empresa.com", tenantId)).thenReturn(true);
        var command = new InviteUserCommand(tenantId, invitedByUserId, "ya-existe@empresa.com", UserRole.EDITOR);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void rejectsInvitingAsSuperAdmin() {
        var command = new InviteUserCommand(tenantId, invitedByUserId, "nuevo@empresa.com", UserRole.SUPER_ADMIN);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.user.invalid_role")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void cancelsAnyExistingPendingInvitationForTheSameEmailBeforeIssuingTheNewOne() {
        UserInvitation existing = UserInvitation.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).email("nuevo@empresa.com")
                .role(UserRole.VIEWER).status(InvitationStatus.PENDING).build();
        when(invitationRepository.existsPendingByEmailAndTenantId("nuevo@empresa.com", tenantId)).thenReturn(true);
        when(invitationRepository.findAllPendingByTenantId(tenantId)).thenReturn(List.of(existing));

        var command = new InviteUserCommand(tenantId, invitedByUserId, "nuevo@empresa.com", UserRole.EDITOR);
        service.execute(command);

        assertThat(existing.getStatus()).isEqualTo(InvitationStatus.CANCELLED);
    }
}
