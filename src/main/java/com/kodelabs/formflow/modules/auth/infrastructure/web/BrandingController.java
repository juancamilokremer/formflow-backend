package com.kodelabs.formflow.modules.auth.infrastructure.web;

import com.kodelabs.formflow.modules.auth.domain.port.in.DeleteLogoUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.GetBrandingUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.UpdateBrandingUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.UploadLogoUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.DeleteLogoCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetBrandingQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.UpdateBrandingCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.UploadLogoCommand;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request.UpdateBrandingRequest;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response.BrandingResponse;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestController
@RequestMapping("/api/v1/tenant/branding")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TENANT_ADMIN')")
@Tag(name = "Branding", description = "Personalización visual del tenant (logo, colores, nombre). Requiere rol TENANT_ADMIN.")
@SecurityRequirement(name = "Bearer Auth")
public class BrandingController {

    private final GetBrandingUseCase getBranding;
    private final UpdateBrandingUseCase updateBranding;
    private final UploadLogoUseCase uploadLogo;
    private final DeleteLogoUseCase deleteLogo;

    @GetMapping
    @Operation(summary = "Obtener el branding actual del tenant")
    public ResponseEntity<ApiResponse<BrandingResponse>> get() {
        var result = getBranding.execute(new GetBrandingQuery(tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(BrandingResponse.from(result)));
    }

    @PutMapping
    @Operation(summary = "Actualizar nombre y colores", description = "No modifica el logo — usar los endpoints de /logo para eso.")
    public ResponseEntity<ApiResponse<BrandingResponse>> update(@Valid @RequestBody UpdateBrandingRequest request) {
        var result = updateBranding.execute(new UpdateBrandingCommand(
                tenantId(), request.name(), request.primaryColor(), request.secondaryColor()));
        return ResponseEntity.ok(ApiResponse.ok(BrandingResponse.from(result)));
    }

    @PostMapping("/logo")
    @Operation(summary = "Subir el logo", description = "PNG/JPG/SVG, máximo 2MB. PNG/JPG se redimensionan a 400x200 si exceden ese tamaño.")
    public ResponseEntity<ApiResponse<BrandingResponse>> uploadLogo(
            @RequestParam("file") MultipartFile file) throws IOException {
        UUID fileId = UUID.randomUUID();
        String logoUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/public/files/{fileId}")
                .buildAndExpand(fileId)
                .toUriString();

        var result = uploadLogo.execute(new UploadLogoCommand(
                tenantId(), fileId, logoUrl, file.getOriginalFilename(), file.getBytes()));
        return ResponseEntity.ok(ApiResponse.ok(BrandingResponse.from(result)));
    }

    @DeleteMapping("/logo")
    @Operation(summary = "Eliminar el logo", description = "Vuelve al branding por defecto (sin logo).")
    public ResponseEntity<ApiResponse<BrandingResponse>> deleteLogo() {
        var result = deleteLogo.execute(new DeleteLogoCommand(tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(BrandingResponse.from(result)));
    }
}
