package com.kodelabs.formflow.modules.forms.infrastructure.web;

import com.kodelabs.formflow.modules.forms.domain.port.in.DownloadAnswerFileUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.UploadAnswerFileUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.DownloadAnswerFileCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UploadAnswerFileCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.DownloadAnswerFileResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.UploadAnswerFileResult;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.UploadedFileResponse;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Tag(name = "Archivos Públicos", description = "Subida y descarga de archivos de respuestas tipo 'file'. Sin autenticación requerida.")
public class PublicFileController {

    private final UploadAnswerFileUseCase uploadAnswerFile;
    private final DownloadAnswerFileUseCase downloadAnswerFile;

    @PostMapping("/forms/{formId}/questions/{questionId}/files")
    @Operation(
            summary = "Subir el archivo de una respuesta tipo 'file'",
            description = "Valida tamaño y extensión contra la configuración de la pregunta. " +
                    "Retorna la URL absoluta a usar como value de la respuesta.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Archivo guardado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Archivo demasiado grande o tipo no permitido", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Formulario o pregunta no encontrados", content = @Content)
    public ResponseEntity<ApiResponse<UploadedFileResponse>> upload(
            @PathVariable UUID formId,
            @PathVariable UUID questionId,
            @RequestParam("file") MultipartFile file) throws IOException {

        UploadAnswerFileResult result = uploadAnswerFile.execute(new UploadAnswerFileCommand(
                formId, questionId, file.getOriginalFilename(), file.getBytes()));

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/public/files/{fileId}")
                .buildAndExpand(result.fileId())
                .toUriString();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(new UploadedFileResponse(url)));
    }

    @GetMapping("/files/{fileId}")
    @Operation(summary = "Descargar un archivo subido como respuesta")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Archivo encontrado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Archivo no encontrado", content = @Content)
    public ResponseEntity<byte[]> download(@PathVariable UUID fileId) {
        DownloadAnswerFileResult file = downloadAnswerFile.execute(new DownloadAnswerFileCommand(fileId));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename() + "\"")
                .body(file.content());
    }
}
