package com.auto.app.easycar.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@Schema(name = "TipoMantenimientoResumen",
        description = "Tipo de mantenimiento embebido en el vehiculo. No incluye el vehiculo para evitar recursion")
public record TipoMantenimientoResumen(

        @Schema(description = "Identificador del tipo de mantenimiento")
        Long id,

        @Schema(description = "Nombre del mantenimiento", example = "Cambio de aceite")
        String nombreMantenimiento,

        @Schema(description = "Fecha del siguiente mantenimiento")
        Date fechaSiguienteMantenimiento,

        @Schema(description = "Observaciones")
        String observacion,

        @Schema(description = "Kilometraje objetivo", example = "50000")
        Integer kilometrajeObjetivo,

        @Schema(description = "Tipo de vista", example = "1")
        Integer tipoVista
) {
}
