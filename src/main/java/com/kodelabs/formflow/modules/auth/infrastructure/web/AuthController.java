package com.kodelabs.formflow.modules.auth.infrastructure.web;

import com.kodelabs.formflow.modules.auth.domain.port.in.LoginUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.RefreshTokenUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.RegisterTenantUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.LoginCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.RefreshTokenCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.RegisterTenantCommand;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.AuthResponse;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.LoginRequest;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.RefreshTokenRequest;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.RegisterRequest;
import com.kodelabs.formflow.shared.i18n.Messages;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inbound adapter: depends only on the input ports (interfaces),
 * never on the use case implementations.
 *
 * @SecurityRequirements (empty) clears the global Bearer requirement so these
 * public endpoints render without the padlock icon in Swagger UI.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro de empresas, login y rotación de tokens JWT. Endpoints públicos — no requieren Bearer token.")
@SecurityRequirements
public class AuthController {

    private final RegisterTenantUseCase registerTenantUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final Messages messages;

    @PostMapping("/register")
    @Operation(
            summary = "Registrar una nueva empresa junto con su usuario administrador",
            description = "Crea el tenant con plan FREE, su usuario TENANT_ADMIN con la contraseña " +
                    "hasheada (BCrypt) y devuelve el par de tokens de la sesión inicial.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "Empresa registrada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Datos de entrada inválidos", content = @io.swagger.v3.oas.annotations.media.Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "Ya existe una empresa con ese slug", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        var result = registerTenantUseCase.execute(new RegisterTenantCommand(
                request.companyName(), request.slug(), request.email(),
                request.password(), request.firstName(), request.lastName()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(messages.get("success.tenant.registered"), AuthResponse.from(result)));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Autenticar un usuario con email, contraseña y slug de la empresa",
            description = "Devuelve un access token JWT (claims: userId, tenantId, email, role) y un " +
                    "refresh token opaco de un solo uso. Ante cualquier credencial incorrecta la " +
                    "respuesta es el mismo 401 genérico — no revela qué dato falló.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Autenticación exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Credenciales inválidas", content = @io.swagger.v3.oas.annotations.media.Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "La empresa está suspendida o cancelada", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        var result = loginUseCase.execute(new LoginCommand(
                request.tenantSlug(), request.email(), request.password()));
        return ResponseEntity.ok(ApiResponse.ok(AuthResponse.from(result)));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Rotar tokens: invalida el refresh token usado y emite un nuevo par",
            description = "Rotación de un solo uso. Si se reutiliza un refresh token ya rotado se asume " +
                    "robo y se revocan TODOS los tokens activos del usuario.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Tokens rotados exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Refresh token inválido, expirado o reutilizado", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        var result = refreshTokenUseCase.execute(new RefreshTokenCommand(request.refreshToken()));
        return ResponseEntity.ok(ApiResponse.ok(AuthResponse.from(result)));
    }
}
