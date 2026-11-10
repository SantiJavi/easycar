package com.auto.app.easycar.controller;

import com.auto.app.easycar.dto.HistorialMantenimientoRequest;
import com.auto.app.easycar.dto.HistorialMantenimientoResponse;
import com.auto.app.easycar.exceptions.ApiError;
import com.auto.app.easycar.services.HistorialMantenimientoService;
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
@RequestMapping("/api/historiales")
@Tag(name = "Historial de mantenimientos", description = "Registro historico de mantenimientos realizados")
@SecurityRequirement(name = "bearerAuth")
public class HistorialMantenimientoController {

    private final HistorialMantenimientoService service;

    public HistorialMantenimientoController(HistorialMantenimientoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar todo el historial")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado del historial"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public ResponseEntity<List<HistorialMantenimientoResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Obtener registro del historial por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro encontrado"),
            @ApiResponse(responseCode = "404", description = "Registro no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<HistorialMantenimientoResponse> findById(
            @Parameter(description = "Identificador del registro") @PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Listar historial de un vehiculo")
    @ApiResponse(responseCode = "200", description = "Historial del vehiculo")
    @GetMapping("/vehiculo/{vehiculoId}")
    public ResponseEntity<List<HistorialMantenimientoResponse>> findByVehiculo(
            @Parameter(description = "Identificador del vehiculo") @PathVariable UUID vehiculoId) {
        return ResponseEntity.ok(service.findByVehiculo(vehiculoId));
    }

    @Operation(summary = "Listar historial de un usuario")
    @ApiResponse(responseCode = "200", description = "Historial del usuario")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<HistorialMantenimientoResponse>> findByUsuario(
            @Parameter(description = "Identificador del usuario") @PathVariable UUID usuarioId) {
        return ResponseEntity.ok(service.findByUsuario(usuarioId));
    }

    @Operation(summary = "Listar historial de un tipo de mantenimiento")
    @ApiResponse(responseCode = "200", description = "Historial del tipo de mantenimiento")
    @GetMapping("/tipo-mantenimiento/{tipoMantenimientoId}")
    public ResponseEntity<List<HistorialMantenimientoResponse>> findByTipoMantenimiento(
            @Parameter(description = "Identificador del tipo de mantenimiento") @PathVariable Long tipoMantenimientoId) {
        return ResponseEntity.ok(service.findByTipoMantenimiento(tipoMantenimientoId));
    }

    @Operation(summary = "Crear registro del historial")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registro creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Usuario, vehiculo o tipo de mantenimiento no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<HistorialMantenimientoResponse> create(
            @Valid @RequestBody HistorialMantenimientoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Actualizar registro del historial")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Registro o referencia no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<HistorialMantenimientoResponse> update(
            @Parameter(description = "Identificador del registro") @PathVariable Long id,
            @Valid @RequestBody HistorialMantenimientoRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(summary = "Eliminar registro del historial")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro eliminado"),
            @ApiResponse(responseCode = "404", description = "Registro no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador del registro") @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
