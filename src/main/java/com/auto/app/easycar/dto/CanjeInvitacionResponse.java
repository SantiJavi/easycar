package com.auto.app.easycar.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "CanjeInvitacionResponse", description = "Vehiculo al que se accedio con un codigo")
public record CanjeInvitacionResponse(

        @Schema(description = "Vehiculo asociado")
        UUID vehiculoId,

        @Schema(description = "Placa del vehiculo", example = "ABC123")
        String placa,

        @Schema(description = "Nombre o alias del vehiculo", example = "Mi carro")
        String nombre,

        @Schema(description = "Rol obtenido", example = "COLABORADOR")
        String rol
) {
}
