package com.auto.app.easycar.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(name = "ApiError", description = "Cuerpo de error estandar de la API")
public record ApiError(

        @Schema(description = "Momento en que ocurrio el error")
        LocalDateTime timestamp,

        @Schema(description = "Codigo HTTP", example = "404")
        int status,

        @Schema(description = "Descripcion corta del error", example = "Not Found")
        String error,

        @Schema(description = "Detalle del error", example = "No se encontro el vehiculo con id ...")
        String message,

        @Schema(description = "Ruta solicitada", example = "/api/vehiculos/1")
        String path,

        @Schema(description = "Errores de validacion por campo (solo en respuestas 400)")
        Map<String, String> fieldErrors
) {
}
