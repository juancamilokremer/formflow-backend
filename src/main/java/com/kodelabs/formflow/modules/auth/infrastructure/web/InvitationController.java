package com.kodelabs.formflow.modules.auth.infrastructure.web;

import com.kodelabs.formflow.modules.auth.domain.port.in.AcceptInvitationUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.GetInvitationUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.AcceptInvitationCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetInvitationQuery;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request.AcceptInvitationRequest;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response.AcceptInvitationResponse;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response.InvitationPreviewResponse;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public — no auth. The person accepting doesn't have an account yet.
 */
@RestController
@RequestMapping("/api/v1/public/invitations")
@RequiredArgsConstructor
@Tag(name = "Invitaciones Públicas", description = "Verificación y aceptación de invitaciones de usuario. Sin autenticación requerida.")
public class InvitationController {

    private final GetInvitationUseCase getInvitation;
    private final AcceptInvitationUseCase acceptInvitation;

    @GetMapping("/{token}")
    @Operation(summary = "Verificar un token de invitación", description = "404 si no existe/fue cancelada, 410 si expiró, 409 si ya fue aceptada.")
    public ResponseEntity<ApiResponse<InvitationPreviewResponse>> get(@PathVariable String token) {
        var result = getInvitation.execute(new GetInvitationQuery(token));
        return ResponseEntity.ok(ApiResponse.ok(InvitationPreviewResponse.from(result)));
    }

    @PostMapping("/{token}/accept")
    @Operation(summary = "Aceptar una invitación y crear la cuenta")
    public ResponseEntity<ApiResponse<AcceptInvitationResponse>> accept(
            @PathVariable String token, @Valid @RequestBody AcceptInvitationRequest request) {
        var result = acceptInvitation.execute(new AcceptInvitationCommand(
                token, request.firstName(), request.lastName(), request.password()));
        return ResponseEntity.ok(ApiResponse.ok(AcceptInvitationResponse.from(result)));
    }
}
