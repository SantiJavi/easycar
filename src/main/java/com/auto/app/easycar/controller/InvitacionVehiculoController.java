package com.auto.app.easycar.controller;

import com.auto.app.easycar.dto.CanjeInvitacionResponse;
import com.auto.app.easycar.dto.InvitacionRequest;
import com.auto.app.easycar.dto.InvitacionResponse;
import com.auto.app.easycar.dto.RenovarInvitacionRequest;
import com.auto.app.easycar.exceptions.ApiError;
import com.auto.app.easycar.services.InvitacionVehiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/invitaciones")
@Tag(name = "Invitaciones", description = "Codigos para compartir un vehiculo y gestionar sus accesos")
@SecurityRequirement(name = "bearerAuth")
public class InvitacionVehiculoController {

    private final InvitacionVehiculoService service;

    public InvitacionVehiculoController(InvitacionVehiculoService service) {
        this.service = service;
    }

    @Operation(summary = "Generar un codigo de acceso",
            description = "Solo el propietario. La vigencia la elige quien comparte (1 a 365 dias)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Codigo generado"),
            @ApiResponse(responseCode = "403", description = "No eres el propietario",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Vehiculo no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<InvitacionResponse> crear(@Valid @RequestBody InvitacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @Operation(summary = "Listar los codigos de un vehiculo, con su estado")
    @ApiResponse(responseCode = "200", description = "Codigos del vehiculo")
    @GetMapping("/vehiculo/{vehiculoId}")
    public ResponseEntity<List<InvitacionResponse>> listarPorVehiculo(
            @Parameter(description = "Identificador del vehiculo") @PathVariable UUID vehiculoId) {
        return ResponseEntity.ok(service.listarPorVehiculo(vehiculoId));
    }

    @Operation(summary = "Renovar un codigo existente",
            description = "Vuelve a correr la vigencia desde hoy, sin cambiar el codigo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Codigo renovado"),
            @ApiResponse(responseCode = "410", description = "El codigo fue revocado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}/renovar")
    public ResponseEntity<InvitacionResponse> renovar(
            @Parameter(description = "Identificador de la invitacion") @PathVariable Long id,
            @Valid @RequestBody RenovarInvitacionRequest request) {
        return ResponseEntity.ok(service.renovar(id, request));
    }

    @Operation(summary = "Revocar un codigo",
            description = "El codigo deja de servir de inmediato; quien ya lo canjeo conserva el acceso")
    @ApiResponse(responseCode = "204", description = "Codigo revocado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revocar(
            @Parameter(description = "Identificador de la invitacion") @PathVariable Long id) {
        service.revocar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Canjear un codigo de acceso",
            description = "Crea el acceso del usuario autenticado al vehiculo compartido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Acceso otorgado"),
            @ApiResponse(responseCode = "404", description = "Codigo inexistente",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Ya tienes acceso a ese vehiculo",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "410", description = "Codigo vencido o revocado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/canjear/{codigo}")
    public ResponseEntity<CanjeInvitacionResponse> canjear(
            @Parameter(description = "Codigo de acceso", example = "K7M2PQX9") @PathVariable String codigo) {
        return ResponseEntity.ok(service.canjear(codigo));
    }
}
