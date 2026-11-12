package com.auto.app.easycar.controller;

import com.auto.app.easycar.dto.AccesoVehiculoResponse;
import com.auto.app.easycar.dto.CambiarRolRequest;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehiculos/{vehiculoId}/accesos")
@Tag(name = "Accesos del vehiculo", description = "Personas con acceso a un vehiculo compartido")
@SecurityRequirement(name = "bearerAuth")
public class AccesoVehiculoController {

    private final InvitacionVehiculoService service;

    public AccesoVehiculoController(InvitacionVehiculoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar las personas con acceso al vehiculo")
    @ApiResponse(responseCode = "200", description = "Accesos del vehiculo")
    @GetMapping
    public ResponseEntity<List<AccesoVehiculoResponse>> listar(
            @Parameter(description = "Identificador del vehiculo") @PathVariable UUID vehiculoId) {
        return ResponseEntity.ok(service.listarAccesos(vehiculoId));
    }

    @Operation(summary = "Cambiar el rol de un colaborador",
            description = "Solo el propietario. Valores validos: COLABORADOR o LECTURA")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol actualizado"),
            @ApiResponse(responseCode = "400", description = "Rol invalido o intento de cambiar al propietario",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "No eres el propietario",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{usuarioId}")
    public ResponseEntity<AccesoVehiculoResponse> cambiarRol(
            @Parameter(description = "Identificador del vehiculo") @PathVariable UUID vehiculoId,
            @Parameter(description = "Identificador del usuario") @PathVariable UUID usuarioId,
            @Valid @RequestBody CambiarRolRequest request) {
        return ResponseEntity.ok(service.cambiarRol(vehiculoId, usuarioId, request));
    }

    @Operation(summary = "Quitar el acceso de una persona",
            description = "El propietario puede expulsar a cualquiera; un colaborador puede salirse a si mismo. "
                    + "Los registros de historial que haya creado se conservan")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Acceso retirado"),
            @ApiResponse(responseCode = "403", description = "No tienes permiso para quitar ese acceso",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "El usuario no tiene acceso al vehiculo",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> quitar(
            @Parameter(description = "Identificador del vehiculo") @PathVariable UUID vehiculoId,
            @Parameter(description = "Identificador del usuario") @PathVariable UUID usuarioId) {
        service.quitarAcceso(vehiculoId, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
