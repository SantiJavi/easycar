package com.auto.app.easycar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(name = "KilometrajeRequest", description = "Nuevo kilometraje del vehiculo")
public record KilometrajeRequest(

        @Schema(description = "Kilometraje actual", example = "125500")
        @NotNull(message = "El kilometraje es obligatorio")
        @PositiveOrZero(message = "El kilometraje no puede ser negativo")
        Integer kilometrajeActual
) {
}
