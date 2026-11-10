package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.TipoMantenimientoRequest;
import com.auto.app.easycar.dto.TipoMantenimientoResponse;

import java.util.List;
import java.util.UUID;

public interface TipoMantenimientoService {

    List<TipoMantenimientoResponse> findAll();

    TipoMantenimientoResponse findById(Long id);

    List<TipoMantenimientoResponse> findByVehiculo(UUID vehiculoId);

    TipoMantenimientoResponse create(TipoMantenimientoRequest request);

    TipoMantenimientoResponse update(Long id, TipoMantenimientoRequest request);

    void delete(Long id);
}
