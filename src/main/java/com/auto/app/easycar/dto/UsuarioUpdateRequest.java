package com.auto.app.easycar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "UsuarioUpdateRequest", description = "Datos para actualizar un usuario. La contrasena es opcional")
public record UsuarioUpdateRequest(

        @Schema(description = "Nombre de usuario", example = "santiago", maxLength = 100)
        @NotBlank(message = "El usuario es obligatorio")
        @Size(max = 100, message = "El usuario no puede superar los 100 caracteres")
        String usuario,

        @Schema(description = "Correo electronico unico del usuario", example = "santiago@easycar.com", maxLength = 100)
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        @Size(max = 100, message = "El email no puede superar los 100 caracteres")
        String email,

        @Schema(description = "Nueva contrasena en texto plano. Si se omite, se conserva la actual", example = "Easycar456", minLength = 8)
        @Size(min = 8, max = 100, message = "La contrasena debe tener entre 8 y 100 caracteres")
        String password
) {
}
