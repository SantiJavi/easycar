package com.auto.app.easycar.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "UserVehiculoResponse", description = "Asociacion entre un usuario y un vehiculo")
public record UserVehiculoResponse(

        @Schema(description = "Identificador de la asociacion")
        Long id,

        @Schema(description = "Identificador del usuario")
        UUID usuarioId,

        @Schema(description = "Identificador del vehiculo")
        UUID vehiculoId
) {
}
