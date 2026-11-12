package com.auto.app.easycar.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.UUID;

@Schema(name = "AccesoVehiculoResponse", description = "Persona con acceso a un vehiculo y su rol")
public record AccesoVehiculoResponse(

        @Schema(description = "Identificador del usuario")
        UUID usuarioId,

        @Schema(description = "Nombre de usuario", example = "santiago")
        String usuario,

        @Schema(description = "Email del usuario")
        String email,

        @Schema(description = "PROPIETARIO, COLABORADOR o LECTURA", example = "COLABORADOR")
        String rol,

        @Schema(description = "Momento en que se le dio acceso")
        Date fechaAlta
) {
}
