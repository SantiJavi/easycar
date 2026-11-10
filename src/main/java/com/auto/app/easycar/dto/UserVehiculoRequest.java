package com.auto.app.easycar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(name = "UserVehiculoRequest", description = "Datos para asociar un usuario con un vehiculo")
public record UserVehiculoRequest(

        @Schema(description = "Identificador del usuario")
        @NotNull(message = "El usuario es obligatorio")
        UUID usuarioId,

        @Schema(description = "Identificador del vehiculo")
        @NotNull(message = "El vehiculo es obligatorio")
        UUID vehiculoId
) {
}
