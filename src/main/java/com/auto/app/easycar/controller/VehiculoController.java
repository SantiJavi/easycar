package com.auto.app.easycar.controller;

import com.auto.app.easycar.dto.KilometrajeRequest;
import com.auto.app.easycar.dto.VehiculoRequest;
import com.auto.app.easycar.dto.VehiculoResponse;
import com.auto.app.easycar.exceptions.ApiError;
import com.auto.app.easycar.services.VehiculoService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehiculos")
@Tag(name = "Vehiculos", description = "CRUD de vehiculos de EasyCar")
@SecurityRequirement(name = "bearerAuth")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @Operation(summary = "Listar vehiculos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de vehiculos"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public ResponseEntity<List<VehiculoResponse>> findAll() {
        return ResponseEntity.ok(vehiculoService.findAll());
    }

    @Operation(summary = "Obtener vehiculo por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehiculo encontrado"),
            @ApiResponse(responseCode = "404", description = "Vehiculo no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<VehiculoResponse> findById(
            @Parameter(description = "Identificador del vehiculo") @PathVariable UUID id) {
        return ResponseEntity.ok(vehiculoService.findById(id));
    }

    @Operation(summary = "Obtener vehiculo por placa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehiculo encontrado"),
            @ApiResponse(responseCode = "404", description = "Vehiculo no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/placa/{placa}")
    public ResponseEntity<VehiculoResponse> findByPlaca(
            @Parameter(description = "Placa del vehiculo", example = "ABC123") @PathVariable String placa) {
        return ResponseEntity.ok(vehiculoService.findByPlaca(placa));
    }

    @Operation(summary = "Buscar vehiculos por marca",
            description = "Busqueda parcial e insensible a mayusculas")
    @ApiResponse(responseCode = "200", description = "Coincidencias encontradas")
    @GetMapping("/buscar")
    public ResponseEntity<List<VehiculoResponse>> findByMarca(
            @Parameter(description = "Texto a buscar en la marca", example = "Toy") @RequestParam String marca) {
        return ResponseEntity.ok(vehiculoService.findByMarca(marca));
    }

    @Operation(summary = "Crear vehiculo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vehiculo creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "La placa ya existe",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<VehiculoResponse> create(@Valid @RequestBody VehiculoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculoService.create(request));
    }

    @Operation(summary = "Actualizar vehiculo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehiculo actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Vehiculo no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "La placa ya existe",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<VehiculoResponse> update(
            @Parameter(description = "Identificador del vehiculo") @PathVariable UUID id,
            @Valid @RequestBody VehiculoRequest request) {
        return ResponseEntity.ok(vehiculoService.update(id, request));
    }

    @Operation(summary = "Actualizar solo el kilometraje",
            description = "No crea registros de historial ni toca los mantenimientos programados. "
                    + "Acepta cualquier valor no negativo, incluso menor al anterior: sirve para "
                    + "corregir un error de captura.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kilometraje actualizado"),
            @ApiResponse(responseCode = "400", description = "Kilometraje invalido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Vehiculo no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping("/{id}/kilometraje")
    public ResponseEntity<VehiculoResponse> actualizarKilometraje(
            @Parameter(description = "Identificador del vehiculo") @PathVariable UUID id,
            @Valid @RequestBody KilometrajeRequest request) {
        return ResponseEntity.ok(vehiculoService.actualizarKilometraje(id, request.kilometrajeActual()));
    }

    @Operation(summary = "Eliminar vehiculo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Vehiculo eliminado"),
            @ApiResponse(responseCode = "404", description = "Vehiculo no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "El vehiculo tiene registros asociados",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador del vehiculo") @PathVariable UUID id) {
        vehiculoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
