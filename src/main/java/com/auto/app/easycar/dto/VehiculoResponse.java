package com.auto.app.easycar.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(name = "VehiculoResponse",
        description = "Vehiculo con sus tipos de mantenimiento e historial. Los hijos no referencian al vehiculo")
public record VehiculoResponse(

        @Schema(description = "Identificador del vehiculo")
        UUID id,

        @Schema(description = "Placa del vehiculo", example = "ABC123")
        String placa,

        @Schema(description = "Nombre o alias del vehiculo", example = "Mi carro")
        String nombre,

        @Schema(description = "Marca del vehiculo", example = "Toyota")
        String marca,

        @Schema(description = "Fecha de matricula", example = "2023-05-15")
        LocalDate fechaMatricula,

        @Schema(description = "Kilometraje actual del vehiculo", example = "125000")
        Integer kilometrajeActual,

        @Schema(description = "Tipos de mantenimiento del vehiculo (Vehiculo -> TipoMantenimiento, sin vuelta)")
        List<TipoMantenimientoResumen> tiposMantenimiento,

        @Schema(description = "Historial de mantenimientos del vehiculo (Vehiculo -> Historial, sin vuelta)")
        List<HistorialResumen> historial
) {
}
