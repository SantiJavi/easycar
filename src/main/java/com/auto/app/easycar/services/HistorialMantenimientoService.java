package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.HistorialMantenimientoRequest;
import com.auto.app.easycar.dto.HistorialMantenimientoResponse;

import java.util.List;
import java.util.UUID;

public interface HistorialMantenimientoService {

    List<HistorialMantenimientoResponse> findAll();

    HistorialMantenimientoResponse findById(Long id);

    List<HistorialMantenimientoResponse> findByVehiculo(UUID vehiculoId);

    List<HistorialMantenimientoResponse> findByUsuario(UUID usuarioId);

    List<HistorialMantenimientoResponse> findByTipoMantenimiento(Long tipoMantenimientoId);

    HistorialMantenimientoResponse create(HistorialMantenimientoRequest request);

    HistorialMantenimientoResponse update(Long id, HistorialMantenimientoRequest request);

    void delete(Long id);
}
