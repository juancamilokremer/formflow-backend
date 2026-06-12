package com.kodelabs.formflow.modules.auth.infrastructure.web;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.LoginCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.LoginUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.RefreshTokenCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.RefreshTokenUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.RegisterTenantCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.RegisterTenantUseCase;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.AuthResponse;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.LoginRequest;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.RefreshTokenRequest;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.RegisterRequest;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
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
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registro de empresas, login y rotación de tokens")
public class AuthController {

    private final RegisterTenantUseCase registerTenantUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    @PostMapping("/register")
    @Operation(summary = "Registrar una nueva empresa junto con su usuario administrador")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        var result = registerTenantUseCase.execute(new RegisterTenantCommand(
                request.companyName(), request.slug(), request.email(),
                request.password(), request.firstName(), request.lastName()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Empresa registrada exitosamente", AuthResponse.from(result)));
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar un usuario con email, contraseña y slug de la empresa")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        var result = loginUseCase.execute(new LoginCommand(
                request.tenantSlug(), request.email(), request.password()));
        return ResponseEntity.ok(ApiResponse.ok(AuthResponse.from(result)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotar tokens: invalida el refresh token usado y emite un nuevo par")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        var result = refreshTokenUseCase.execute(new RefreshTokenCommand(request.refreshToken()));
        return ResponseEntity.ok(ApiResponse.ok(AuthResponse.from(result)));
    }
}
