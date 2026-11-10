package com.auto.app.easycar.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(name = "UsuarioResponse",
        description = "Usuario registrado con su historial. Nunca incluye la contrasena")
public record UsuarioResponse(

        @Schema(description = "Identificador del usuario")
        UUID id,

        @Schema(description = "Nombre de usuario", example = "santiago")
        String usuario,

        @Schema(description = "Correo electronico", example = "santiago@easycar.com")
        String email,

        @Schema(description = "Historial de mantenimientos del usuario (Usuario -> Historial, sin vuelta)")
        List<HistorialResumen> historial
) {
}
