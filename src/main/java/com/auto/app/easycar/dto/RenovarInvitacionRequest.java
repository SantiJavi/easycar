package com.auto.app.easycar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(name = "RenovarInvitacionRequest", description = "Nueva vigencia para un codigo ya existente")
public record RenovarInvitacionRequest(

        @Schema(description = "Dias de vigencia desde ahora (1 a 365)", example = "30")
        @NotNull(message = "Los dias de vigencia son obligatorios")
        @Min(value = 1, message = "La vigencia minima es 1 dia")
        @Max(value = 365, message = "La vigencia maxima es 365 dias")
        Integer diasValidez
) {
}
