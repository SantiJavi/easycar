package com.auto.app.easycar.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.UUID;

@Schema(name = "InvitacionResponse", description = "Codigo de acceso compartido a un vehiculo")
public record InvitacionResponse(

        @Schema(description = "Identificador de la invitacion")
        Long id,

        @Schema(description = "Codigo para compartir", example = "K7M2PQX9")
        String codigo,

        @Schema(description = "Vehiculo compartido")
        UUID vehiculoId,

        @Schema(description = "Momento en que se genero")
        Date fechaCreacion,

        @Schema(description = "Momento en que deja de servir")
        Date expiraEn,

        @Schema(description = "VIGENTE, EXPIRADA o REVOCADA", example = "VIGENTE")
        String estado,

        @Schema(description = "Dias que le quedan al codigo (0 si ya no sirve)", example = "7")
        long diasRestantes
) {
}
