package com.auto.app.easycar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(name = "VehiculoRequest", description = "Datos para crear o actualizar un vehiculo")
public record VehiculoRequest(

        @Schema(description = "Placa del vehiculo", example = "ABC123")
        @NotBlank(message = "La placa es obligatoria")
        @Size(max = 255, message = "La placa no puede superar los 255 caracteres")
        String placa,

        @Schema(description = "Nombre o alias del vehiculo", example = "Mi carro")
        @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
        String nombre,

        @Schema(description = "Marca del vehiculo", example = "Toyota")
        @NotBlank(message = "La marca es obligatoria")
        @Size(max = 255, message = "La marca no puede superar los 255 caracteres")
        String marca,

        @Schema(description = "Fecha de matricula", example = "2023-05-15")
        @PastOrPresent(message = "La fecha de matricula no puede ser futura")
        LocalDate fechaMatricula,

        @Schema(description = "Kilometraje actual del vehiculo", example = "125000")
        @PositiveOrZero(message = "El kilometraje no puede ser negativo")
        Integer kilometrajeActual
) {
}
