package com.auto.app.easycar.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LoginResponse", description = "Token JWT emitido al iniciar sesion")
public record LoginResponse(

        @Schema(description = "Token JWT. Debe enviarse en el header Authorization", example = "eyJhbGciOiJIUzI1NiJ9...")
        String token,

        @Schema(description = "Tipo de token", example = "Bearer")
        String tokenType,

        @Schema(description = "Vigencia del token en segundos", example = "86400")
        long expiresIn,

        @Schema(description = "Identificador del usuario autenticado")
        java.util.UUID usuarioId,

        @Schema(description = "Nombre de usuario", example = "santiago")
        String usuario,

        @Schema(description = "Email autenticado", example = "santiago@easycar.com")
        String email
) {
}
