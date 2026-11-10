package com.auto.app.easycar.controller;

import com.auto.app.easycar.dto.TipoMantenimientoRequest;
import com.auto.app.easycar.dto.TipoMantenimientoResponse;
import com.auto.app.easycar.exceptions.ApiError;
import com.auto.app.easycar.services.TipoMantenimientoService;
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
@RequestMapping("/api/tipos-mantenimiento")
@Tag(name = "Tipos de mantenimiento", description = "Mantenimientos programados por vehiculo")
@SecurityRequirement(name = "bearerAuth")
public class TipoMantenimientoController {

    private final TipoMantenimientoService service;

    public TipoMantenimientoController(TipoMantenimientoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar tipos de mantenimiento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de tipos de mantenimiento"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public ResponseEntity<List<TipoMantenimientoResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Obtener tipo de mantenimiento por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro encontrado"),
            @ApiResponse(responseCode = "404", description = "Registro no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<TipoMantenimientoResponse> findById(
            @Parameter(description = "Identificador del tipo de mantenimiento") @PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Listar tipos de mantenimiento de un vehiculo")
    @ApiResponse(responseCode = "200", description = "Listado de tipos de mantenimiento del vehiculo")
    @GetMapping("/vehiculo/{vehiculoId}")
    public ResponseEntity<List<TipoMantenimientoResponse>> findByVehiculo(
            @Parameter(description = "Identificador del vehiculo") @PathVariable UUID vehiculoId) {
        return ResponseEntity.ok(service.findByVehiculo(vehiculoId));
    }

    @Operation(summary = "Crear tipo de mantenimiento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registro creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Vehiculo no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<TipoMantenimientoResponse> create(@Valid @RequestBody TipoMantenimientoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Actualizar tipo de mantenimiento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Registro o vehiculo no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<TipoMantenimientoResponse> update(
            @Parameter(description = "Identificador del tipo de mantenimiento") @PathVariable Long id,
            @Valid @RequestBody TipoMantenimientoRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(summary = "Eliminar tipo de mantenimiento")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro eliminado"),
            @ApiResponse(responseCode = "404", description = "Registro no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "El registro tiene historial asociado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador del tipo de mantenimiento") @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
