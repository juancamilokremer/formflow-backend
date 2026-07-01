package com.kodelabs.formflow.modules.forms.infrastructure.web;

import com.kodelabs.formflow.modules.forms.domain.port.in.AddCandidateUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.ImportCandidatesUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.RemoveCandidateUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.AddCandidateCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ImportCandidatesCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.RemoveCandidateCommand;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request.AddCandidateRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.CandidateResponse;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.ImportResponse;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

import static com.kodelabs.formflow.shared.web.ControllerUtils.tenantId;
import static com.kodelabs.formflow.shared.web.ControllerUtils.userId;

@RestController
@RequestMapping("/api/v1/convocatorias")
@RequiredArgsConstructor
@Tag(name = "Candidatos", description = "Gestion de candidatos dentro de una convocatoria. Requiere autenticacion.")
@SecurityRequirement(name = "Bearer Auth")
public class CandidateController {

    private final AddCandidateUseCase addCandidate;
    private final RemoveCandidateUseCase removeCandidate;
    private final ImportCandidatesUseCase importCandidates;

    @PostMapping("/{convocatoriaId}/candidates")
    @Operation(summary = "Agregar candidato", description = "Agrega un candidato a la convocatoria. No se permite duplicar email.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Candidato agregado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email duplicado o convocatoria cerrada", content = @Content)
    public ResponseEntity<ApiResponse<CandidateResponse>> addCandidate(
            @PathVariable UUID convocatoriaId,
            @Valid @RequestBody AddCandidateRequest request,
            Authentication auth) {
        var result = addCandidate.execute(new AddCandidateCommand(
                convocatoriaId, tenantId(), userId(auth), request.name(), request.email()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(CandidateResponse.from(result)));
    }

    @DeleteMapping("/{convocatoriaId}/candidates/{candidateId}")
    @Operation(summary = "Eliminar candidato", description = "Elimina un candidato de la convocatoria.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Candidato eliminado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Convocatoria cerrada", content = @Content)
    public ResponseEntity<ApiResponse<Void>> removeCandidate(
            @PathVariable UUID convocatoriaId,
            @PathVariable UUID candidateId,
            Authentication auth) {
        removeCandidate.execute(new RemoveCandidateCommand(convocatoriaId, candidateId, tenantId(), userId(auth)));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{convocatoriaId}/candidates/import")
    @Operation(summary = "Importar candidatos CSV",
            description = "Importa candidatos desde un CSV (columnas: nombre, email). " +
                    "Emails duplicados en la convocatoria se omiten con aviso.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Importacion completada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "CSV invalido", content = @Content)
    public ResponseEntity<ApiResponse<ImportResponse>> importCandidates(
            @PathVariable UUID convocatoriaId,
            @RequestParam("file") MultipartFile file,
            Authentication auth) throws IOException {
        var result = importCandidates.execute(new ImportCandidatesCommand(
                convocatoriaId, tenantId(), userId(auth), file.getBytes()));
        return ResponseEntity.ok(ApiResponse.ok(ImportResponse.from(result)));
    }
}
