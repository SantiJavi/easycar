package com.auto.app.easycar.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "LoginRequest", description = "Credenciales para obtener el token JWT")
public record LoginRequest(

        @Schema(description = "Email registrado del usuario", example = "santiago@easycar.com")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        @Size(max = 100, message = "El email no puede superar los 100 caracteres")
        String email,

        @Schema(description = "Contrasena del usuario", example = "Easycar123")
        @NotBlank(message = "La contrasena es obligatoria")
        String password
) {
}
