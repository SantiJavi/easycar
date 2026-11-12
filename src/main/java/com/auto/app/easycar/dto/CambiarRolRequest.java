package com.auto.app.easycar.dto;

import com.auto.app.easycar.entitis.RolVehiculo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "CambiarRolRequest", description = "Nuevo rol de un colaborador (COLABORADOR o LECTURA)")
public record CambiarRolRequest(

        @Schema(description = "Rol a asignar", example = "LECTURA")
        @NotNull(message = "El rol es obligatorio")
        RolVehiculo rol
) {
}
