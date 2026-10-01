package com.kodelabs.formflow.modules.auth.infrastructure.web;

import com.kodelabs.formflow.modules.auth.domain.port.in.DeleteAvatarUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.GetMeUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.UpdateMeUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.UploadAvatarUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.DeleteAvatarCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetMeQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.UpdateMeCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.UploadAvatarCommand;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request.UpdateMeRequest;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response.MeResponse;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

import static com.kodelabs.formflow.shared.web.ControllerUtils.tenantId;
import static com.kodelabs.formflow.shared.web.ControllerUtils.userId;

/**
 * The logged-in user's own account — unlike UserController/BrandingController this has no
 * class-level @PreAuthorize role: any authenticated role manages their own account.
 */
@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
@Tag(name = "Mi cuenta", description = "Datos de la cuenta del usuario autenticado (cualquier rol).")
@SecurityRequirement(name = "Bearer Auth")
public class MeController {

    private final GetMeUseCase getMe;
    private final UpdateMeUseCase updateMe;
    private final UploadAvatarUseCase uploadAvatar;
    private final DeleteAvatarUseCase deleteAvatar;

    @GetMapping
    @Operation(summary = "Obtener mis datos")
    public ResponseEntity<ApiResponse<MeResponse>> get(Authentication auth) {
        var result = getMe.execute(new GetMeQuery(userId(auth), tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(MeResponse.from(result)));
    }

    @PutMapping
    @Operation(summary = "Actualizar mi nombre", description = "No permite cambiar el correo.")
    public ResponseEntity<ApiResponse<MeResponse>> update(
            @Valid @RequestBody UpdateMeRequest request, Authentication auth) {
        var result = updateMe.execute(new UpdateMeCommand(userId(auth), tenantId(), request.firstName(), request.lastName()));
        return ResponseEntity.ok(ApiResponse.ok(MeResponse.from(result)));
    }

    @PostMapping("/avatar")
    @Operation(summary = "Subir mi foto de perfil", description = "PNG/JPG, máximo 2MB. Se redimensiona a 400x400 si excede ese tamaño.")
    public ResponseEntity<ApiResponse<MeResponse>> uploadAvatar(
            @RequestParam("file") MultipartFile file, Authentication auth) throws IOException {
        UUID fileId = UUID.randomUUID();
        String avatarUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/public/files/{fileId}")
                .buildAndExpand(fileId)
                .toUriString();

        var result = uploadAvatar.execute(new UploadAvatarCommand(
                userId(auth), tenantId(), fileId, avatarUrl, file.getOriginalFilename(), file.getBytes()));
        return ResponseEntity.ok(ApiResponse.ok(MeResponse.from(result)));
    }

    @DeleteMapping("/avatar")
    @Operation(summary = "Eliminar mi foto de perfil", description = "Vuelve a mostrar iniciales.")
    public ResponseEntity<ApiResponse<MeResponse>> deleteAvatar(Authentication auth) {
        var result = deleteAvatar.execute(new DeleteAvatarCommand(userId(auth), tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(MeResponse.from(result)));
    }
}
