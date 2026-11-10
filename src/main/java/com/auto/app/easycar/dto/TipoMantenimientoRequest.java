package com.auto.app.easycar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.Date;
import java.util.UUID;

@Schema(name = "TipoMantenimientoRequest", description = "Datos para crear o actualizar un tipo de mantenimiento")
public record TipoMantenimientoRequest(

        @Schema(description = "Nombre del mantenimiento", example = "Cambio de aceite")
        @NotBlank(message = "El nombre del mantenimiento es obligatorio")
        @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
        String nombreMantenimiento,

        @Schema(description = "Fecha del siguiente mantenimiento", example = "2026-12-01T08:00:00.000+00:00")
        Date fechaSiguienteMantenimiento,

        @Schema(description = "Observaciones", example = "Usar aceite 5W-30", maxLength = 255)
        @Size(max = 255, message = "La observacion no puede superar los 255 caracteres")
        String observacion,

        @Schema(description = "Kilometraje objetivo para el mantenimiento", example = "50000")
        @PositiveOrZero(message = "El kilometraje objetivo no puede ser negativo")
        Integer kilometrajeObjetivo,

        @Schema(description = "Tipo de vista del mantenimiento (columna de 2 digitos)", example = "1")
        @NotNull(message = "El tipo de vista es obligatorio")
        Integer tipoVista,

        @Schema(description = "Identificador del vehiculo asociado")
        @NotNull(message = "El vehiculo es obligatorio")
        UUID vehiculoId
) {
}
