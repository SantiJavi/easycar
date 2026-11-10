package com.auto.app.easycar.controller;

import com.auto.app.easycar.dto.UserVehiculoRequest;
import com.auto.app.easycar.dto.UserVehiculoResponse;
import com.auto.app.easycar.exceptions.ApiError;
import com.auto.app.easycar.services.UserVehiculoService;
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
@RequestMapping("/api/user-vehiculo")
@Tag(name = "Usuario - Vehiculo", description = "Asociacion entre usuarios y vehiculos")
@SecurityRequirement(name = "bearerAuth")
public class UserVehiculoController {

    private final UserVehiculoService service;

    public UserVehiculoController(UserVehiculoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar todas las asociaciones")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de asociaciones"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public ResponseEntity<List<UserVehiculoResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Obtener asociacion por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asociacion encontrada"),
            @ApiResponse(responseCode = "404", description = "Asociacion no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserVehiculoResponse> findById(
            @Parameter(description = "Identificador de la asociacion") @PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Listar vehiculos de un usuario")
    @ApiResponse(responseCode = "200", description = "Asociaciones del usuario")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<UserVehiculoResponse>> findByUsuario(
            @Parameter(description = "Identificador del usuario") @PathVariable UUID usuarioId) {
        return ResponseEntity.ok(service.findByUsuario(usuarioId));
    }

    @Operation(summary = "Listar usuarios de un vehiculo")
    @ApiResponse(responseCode = "200", description = "Asociaciones del vehiculo")
    @GetMapping("/vehiculo/{vehiculoId}")
    public ResponseEntity<List<UserVehiculoResponse>> findByVehiculo(
            @Parameter(description = "Identificador del vehiculo") @PathVariable UUID vehiculoId) {
        return ResponseEntity.ok(service.findByVehiculo(vehiculoId));
    }

    @Operation(summary = "Asociar usuario y vehiculo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Asociacion creada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Usuario o vehiculo no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "La asociacion ya existe",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<UserVehiculoResponse> create(@Valid @RequestBody UserVehiculoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Actualizar asociacion",
            description = "Cambia el usuario o el vehiculo asociado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asociacion actualizada"),
            @ApiResponse(responseCode = "404", description = "Asociacion, usuario o vehiculo no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "La asociacion ya existe",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserVehiculoResponse> update(
            @Parameter(description = "Identificador de la asociacion") @PathVariable Long id,
            @Valid @RequestBody UserVehiculoRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(summary = "Eliminar asociacion")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Asociacion eliminada"),
            @ApiResponse(responseCode = "404", description = "Asociacion no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador de la asociacion") @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
