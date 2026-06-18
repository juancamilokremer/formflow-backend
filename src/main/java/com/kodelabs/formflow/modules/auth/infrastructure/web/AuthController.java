package com.kodelabs.formflow.modules.auth.infrastructure.web;

import com.kodelabs.formflow.modules.auth.domain.port.in.ForgotPasswordUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.LoginUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.RefreshTokenUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.RegisterTenantUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.ResendVerificationUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.ResetPasswordUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.VerifyEmailUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ForgotPasswordCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.LoginCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.RefreshTokenCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.RegisterTenantCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ResendVerificationCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ResetPasswordCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.VerifyEmailCommand;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response.AuthResponse;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request.ForgotPasswordRequest;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request.LoginRequest;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request.RefreshTokenRequest;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request.RegisterRequest;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response.RegisterResponse;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request.ResetPasswordRequest;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request.VerifyEmailRequest;
import com.kodelabs.formflow.shared.tenant.TenantContext;
import com.kodelabs.formflow.shared.i18n.Messages;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.UUID;
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
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ResendVerificationUseCase resendVerificationUseCase;
    private final Messages messages;

    @PostMapping("/register")
    @Operation(
            summary = "Registrar una nueva empresa junto con su usuario administrador",
            description = "Crea el tenant con plan FREE y su usuario TENANT_ADMIN con la contraseña " +
                    "hasheada (BCrypt). Envía un correo de verificación — el usuario debe confirmar " +
                    "su correo antes de poder iniciar sesión, por lo que esta respuesta NO incluye tokens.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "Empresa registrada, correo de verificación enviado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Datos de entrada inválidos", content = @io.swagger.v3.oas.annotations.media.Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "Ya existe una empresa con ese slug", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        var result = registerTenantUseCase.execute(new RegisterTenantCommand(
                request.companyName(), request.slug(), request.email(),
                request.password(), request.firstName(), request.lastName()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(messages.get("success.tenant.registered"), RegisterResponse.from(result)));
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

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Solicitar el restablecimiento de contraseña",
            description = "Envía un correo con el enlace de restablecimiento (expira en 1 hora). " +
                    "SIEMPRE responde 200, exista o no la cuenta — no revela si un email está registrado.")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        forgotPasswordUseCase.execute(new ForgotPasswordCommand(request.tenantSlug(), request.email()));
        return ResponseEntity.ok(ApiResponse.ok(messages.get("success.auth.forgot_password"), null));
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Restablecer la contraseña con el token del correo",
            description = "Token de un solo uso. Al cambiar la contraseña se revocan todos los " +
                    "refresh tokens activos del usuario (cierra cualquier sesión robada).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Contraseña actualizada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Token inválido, usado o expirado", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.execute(new ResetPasswordCommand(request.token(), request.newPassword()));
        return ResponseEntity.ok(ApiResponse.ok(messages.get("success.auth.password_reset"), null));
    }

    @PostMapping("/verify-email")
    @Operation(
            summary = "Confirmar el correo con el token de verificación",
            description = "Token de un solo uso que expira en 24 horas.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Correo verificado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Token inválido, usado o expirado", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        verifyEmailUseCase.execute(new VerifyEmailCommand(request.token()));
        return ResponseEntity.ok(ApiResponse.ok(messages.get("success.auth.email_verified"), null));
    }

    @PostMapping("/resend-verification")
    @Operation(
            summary = "Reenviar el correo de verificación al usuario autenticado",
            security = @SecurityRequirement(name = "Bearer Auth"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Correo de verificación reenviado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "El correo ya está verificado", content = @io.swagger.v3.oas.annotations.media.Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "No autenticado", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    public ResponseEntity<ApiResponse<Void>> resendVerification(Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        resendVerificationUseCase.execute(new ResendVerificationCommand(userId, tenantId));
        return ResponseEntity.ok(ApiResponse.ok(messages.get("success.auth.verification_sent"), null));
    }
}
