package com.auto.app.easycar.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.UUID;

@Schema(name = "HistorialMantenimientoResponse", description = "Registro del historial de mantenimientos")
public record HistorialMantenimientoResponse(

        @Schema(description = "Identificador del registro")
        Long id,

        @Schema(description = "Fecha en que se realizo el mantenimiento")
        Date fechaMantenimiento,

        @Schema(description = "Kilometraje del vehiculo al momento del mantenimiento", example = "48200")
        Integer kilometrajeActual,

        @Schema(description = "Observaciones del mantenimiento")
        String observacion,

        @Schema(description = "Identificador del tipo de mantenimiento")
        Long tipoMantenimientoId,

        @Schema(description = "Identificador del usuario")
        UUID usuarioId,

        @Schema(description = "Identificador del vehiculo")
        UUID vehiculoId
) {
}
