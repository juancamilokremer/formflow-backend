package com.kodelabs.formflow.modules.auth.infrastructure.web;

import com.kodelabs.formflow.modules.auth.domain.port.in.CancelInvitationUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.ChangeUserRoleUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.InviteUserUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.ListInvitationsUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.ListUsersUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.RevokeUserAccessUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.CancelInvitationCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ChangeUserRoleCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.InviteUserCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ListInvitationsQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ListUsersQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.RevokeUserAccessCommand;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request.ChangeUserRoleRequest;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request.InviteUserRequest;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response.InvitationResponse;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response.UserResponse;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.kodelabs.formflow.shared.web.ControllerUtils.tenantId;
import static com.kodelabs.formflow.shared.web.ControllerUtils.userId;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TENANT_ADMIN')")
@Tag(name = "Usuarios", description = "Gestión de usuarios e invitaciones del tenant. Requiere rol TENANT_ADMIN.")
@SecurityRequirement(name = "Bearer Auth")
public class UserController {

    private final ListUsersUseCase listUsers;
    private final InviteUserUseCase inviteUser;
    private final ListInvitationsUseCase listInvitations;
    private final CancelInvitationUseCase cancelInvitation;
    private final ChangeUserRoleUseCase changeUserRole;
    private final RevokeUserAccessUseCase revokeUserAccess;

    @GetMapping
    @Operation(summary = "Listar usuarios activos del tenant")
    public ResponseEntity<ApiResponse<List<UserResponse>>> list() {
        var results = listUsers.execute(new ListUsersQuery(tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(results.stream().map(UserResponse::from).toList()));
    }

    @PostMapping("/invite")
    @Operation(summary = "Invitar un usuario por email", description = "El enlace de invitación expira en 48 horas.")
    public ResponseEntity<ApiResponse<InvitationResponse>> invite(
            @Valid @RequestBody InviteUserRequest request, Authentication auth) {
        var result = inviteUser.execute(new InviteUserCommand(tenantId(), userId(auth), request.email(), request.role()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(InvitationResponse.from(result)));
    }

    @GetMapping("/invitations")
    @Operation(summary = "Listar invitaciones pendientes")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> listPendingInvitations() {
        var results = listInvitations.execute(new ListInvitationsQuery(tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(results.stream().map(InvitationResponse::from).toList()));
    }

    @DeleteMapping("/invitations/{id}")
    @Operation(summary = "Cancelar una invitación pendiente")
    public ResponseEntity<ApiResponse<Void>> cancelInvitation(@PathVariable UUID id) {
        cancelInvitation.execute(new CancelInvitationCommand(tenantId(), id));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Cambiar el rol de un usuario", description = "Siempre debe quedar al menos un TENANT_ADMIN activo.")
    public ResponseEntity<ApiResponse<UserResponse>> changeRole(
            @PathVariable UUID id, @Valid @RequestBody ChangeUserRoleRequest request) {
        var result = changeUserRole.execute(new ChangeUserRoleCommand(tenantId(), id, request.role()));
        return ResponseEntity.ok(ApiResponse.ok(UserResponse.from(result)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Revocar el acceso de un usuario", description = "No se puede revocar a sí mismo ni al último TENANT_ADMIN.")
    public ResponseEntity<ApiResponse<Void>> revoke(@PathVariable UUID id, Authentication auth) {
        revokeUserAccess.execute(new RevokeUserAccessCommand(tenantId(), userId(auth), id));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
