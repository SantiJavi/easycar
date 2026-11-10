package com.auto.app.easycar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.Date;
import java.util.UUID;

@Schema(name = "HistorialMantenimientoRequest", description = "Datos para crear o actualizar un registro del historial de mantenimientos")
public record HistorialMantenimientoRequest(

        @Schema(description = "Fecha en que se realizo el mantenimiento", example = "2026-10-01T08:00:00.000+00:00")
        @NotNull(message = "La fecha del mantenimiento es obligatoria")
        Date fechaMantenimiento,

        @Schema(description = "Kilometraje del vehiculo al momento del mantenimiento", example = "48200")
        @PositiveOrZero(message = "El kilometraje actual no puede ser negativo")
        Integer kilometrajeActual,

        @Schema(description = "Observaciones del mantenimiento", example = "Se cambio filtro y aceite")
        @Size(max = 255, message = "La observacion no puede superar los 255 caracteres")
        String observacion,

        @Schema(description = "Identificador del tipo de mantenimiento")
        @NotNull(message = "El tipo de mantenimiento es obligatorio")
        Long tipoMantenimientoId,

        @Schema(description = "Identificador del usuario que realizo el mantenimiento")
        @NotNull(message = "El usuario es obligatorio")
        UUID usuarioId,

        @Schema(description = "Identificador del vehiculo")
        @NotNull(message = "El vehiculo es obligatorio")
        UUID vehiculoId
) {
}
