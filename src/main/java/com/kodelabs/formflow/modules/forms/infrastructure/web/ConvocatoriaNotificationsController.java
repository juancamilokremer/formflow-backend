package com.kodelabs.formflow.modules.forms.infrastructure.web;

import com.kodelabs.formflow.modules.forms.domain.port.in.SendConvocatoriaRemindersUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.SendConvocatoriaRemindersCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SendConvocatoriaRemindersResult;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.kodelabs.formflow.shared.web.ControllerUtils.tenantId;

@RestController
@RequestMapping("/api/v1/convocatorias")
@RequiredArgsConstructor
@Tag(name = "Convocatorias", description = "CRUD y ciclo de vida de convocatorias. Requiere autenticación.")
@SecurityRequirement(name = "Bearer Auth")
public class ConvocatoriaNotificationsController {

    private final SendConvocatoriaRemindersUseCase sendReminders;

    @PostMapping("/{id}/reminders")
    @Operation(summary = "Enviar recordatorios", description = "Envía recordatorio por email a todos los candidatos pendientes de responder.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Recordatorios enviados")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "La convocatoria no está activa", content = @Content)
    public ResponseEntity<ApiResponse<SendConvocatoriaRemindersResult>> sendReminders(@PathVariable UUID id) {
        SendConvocatoriaRemindersResult result = sendReminders.execute(
                new SendConvocatoriaRemindersCommand(id, tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
