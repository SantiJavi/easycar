package com.auto.app.easycar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(name = "InvitacionRequest", description = "Datos para generar un codigo de acceso a un vehiculo")
public record InvitacionRequest(

        @Schema(description = "Vehiculo a compartir")
        @NotNull(message = "El vehiculo es obligatorio")
        UUID vehiculoId,

        @Schema(description = "Dias de vigencia del codigo (1 a 365)", example = "7")
        @NotNull(message = "Los dias de vigencia son obligatorios")
        @Min(value = 1, message = "La vigencia minima es 1 dia")
        @Max(value = 365, message = "La vigencia maxima es 365 dias")
        Integer diasValidez
) {
}
