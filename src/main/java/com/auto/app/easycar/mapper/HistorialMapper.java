package com.auto.app.easycar.mapper;

import com.auto.app.easycar.dto.HistorialResumen;
import com.auto.app.easycar.entitis.HistorialMantenimientos;

import java.util.List;

/**
 * Convierte el historial en su version resumida (sin referencias anidadas),
 * evitando que la serializacion entre en ciclo.
 */
public final class HistorialMapper {

    private HistorialMapper() {
    }

    public static HistorialResumen toResumen(HistorialMantenimientos historial) {
        return new HistorialResumen(
                historial.getId(),
                historial.getFechaMantenimiento(),
                historial.getKilometrajeActual(),
                historial.getObservacion(),
                historial.getFkTipoHistorial().getId(),
                historial.getFkUserHistorial().getId(),
                historial.getFkVehiculoHistorial().getId()
        );
    }

    public static List<HistorialResumen> toResumenes(List<HistorialMantenimientos> historiales) {
        if (historiales == null) {
            return List.of();
        }
        return historiales.stream().map(HistorialMapper::toResumen).toList();
    }
}
